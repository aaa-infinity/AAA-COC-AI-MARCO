"""
Image Detection and Template Matching Engine (similar to Macrorify's Image Matcher).
Supports multi-scale matching, grayscale/color modes, and Non-Maximum Suppression.
"""

import os
import logging
from typing import Tuple, List, Optional, Dict
from PIL import Image

try:
    import cv2
    import numpy as np
    HAS_CV2 = True
except ImportError:
    HAS_CV2 = False
    try:
        import numpy as np
    except ImportError:
        np = None

logger = logging.getLogger(__name__)


class MatchResult:
    def __init__(self, x: int, y: int, w: int, h: int, confidence: float, name: str = ""):
        self.x = x
        self.y = y
        self.w = w
        self.h = h
        self.center_x = x + w // 2
        self.center_y = y + h // 2
        self.confidence = confidence
        self.name = name

    def to_tuple(self) -> Tuple[int, int]:
        return (self.center_x, self.center_y)

    def __repr__(self):
        return f"Match({self.name}, center=({self.center_x}, {self.center_y}), conf={self.confidence:.2f})"


class TemplateMatcher:
    """
    High performance template matching engine with OpenCV and PIL fallback.
    """
    def __init__(self, templates_dir: str = "vision/templates"):
        self.templates_dir = templates_dir
        self.templates = {}
        self._load_templates()

    def _load_templates(self):
        if not os.path.exists(self.templates_dir):
            os.makedirs(self.templates_dir, exist_ok=True)
            return

        for fname in os.listdir(self.templates_dir):
            if fname.lower().endswith((".png", ".jpg", ".jpeg")):
                name = os.path.splitext(fname)[0]
                filepath = os.path.join(self.templates_dir, fname)
                if HAS_CV2:
                    img = cv2.imread(filepath, cv2.IMREAD_COLOR)
                    if img is not None:
                        self.templates[name] = img
                        logger.debug(f"Loaded template '{name}' ({img.shape[1]}x{img.shape[0]})")
                else:
                    try:
                        pil_img = Image.open(filepath).convert("RGB")
                        self.templates[name] = pil_img
                        logger.debug(f"Loaded template '{name}' ({pil_img.size[0]}x{pil_img.size[1]})")
                    except Exception as e:
                        logger.warning(f"Could not load {filepath}: {e}")

    def register_template(self, name: str, img):
        self.templates[name] = img

    def find_template(
        self,
        screen,
        template_name: str,
        threshold: float = 0.80,
        roi: Optional[Tuple[int, int, int, int]] = None,
        scales: List[float] = [1.0]
    ) -> Optional[MatchResult]:
        template = self.templates.get(template_name)
        if template is None:
            logger.debug(f"Template '{template_name}' not found in registry.")
            return None

        if HAS_CV2 and hasattr(screen, "shape") and hasattr(template, "shape"):
            offset_x, offset_y = 0, 0
            search_img = screen
            if roi is not None:
                x1, y1, x2, y2 = roi
                search_img = screen[y1:y2, x1:x2]
                offset_x, offset_y = x1, y1

            best_val = -1.0
            best_loc = None
            best_size = (template.shape[1], template.shape[0])

            screen_gray = cv2.cvtColor(search_img, cv2.COLOR_BGR2GRAY)
            template_gray = cv2.cvtColor(template, cv2.COLOR_BGR2GRAY)

            for scale in scales:
                if scale == 1.0:
                    resized_tmpl = template_gray
                else:
                    new_w = int(template_gray.shape[1] * scale)
                    new_h = int(template_gray.shape[0] * scale)
                    if new_w >= screen_gray.shape[1] or new_h >= screen_gray.shape[0] or new_w < 5 or new_h < 5:
                        continue
                    resized_tmpl = cv2.resize(template_gray, (new_w, new_h))

                res = cv2.matchTemplate(screen_gray, resized_tmpl, cv2.TM_CCOEFF_NORMED)
                min_val, max_val, min_loc, max_loc = cv2.minMaxLoc(res)

                if max_val > best_val:
                    best_val = max_val
                    best_loc = max_loc
                    best_size = (resized_tmpl.shape[1], resized_tmpl.shape[0])

            if best_val >= threshold and best_loc is not None:
                match_x = best_loc[0] + offset_x
                match_y = best_loc[1] + offset_y
                w, h = best_size
                return MatchResult(match_x, match_y, w, h, float(best_val), template_name)

        return None

    def find_all(
        self,
        screen,
        template_name: str,
        threshold: float = 0.78,
        roi: Optional[Tuple[int, int, int, int]] = None,
        max_results: int = 50
    ) -> List[MatchResult]:
        template = self.templates.get(template_name)
        if template is None:
            return []

        if HAS_CV2 and hasattr(screen, "shape") and hasattr(template, "shape"):
            search_img = screen
            offset_x, offset_y = 0, 0
            if roi is not None:
                x1, y1, x2, y2 = roi
                search_img = screen[y1:y2, x1:x2]
                offset_x, offset_y = x1, y1

            screen_gray = cv2.cvtColor(search_img, cv2.COLOR_BGR2GRAY)
            template_gray = cv2.cvtColor(template, cv2.COLOR_BGR2GRAY)
            th, tw = template_gray.shape[:2]

            res = cv2.matchTemplate(screen_gray, template_gray, cv2.TM_CCOEFF_NORMED)
            loc = np.where(res >= threshold)

            boxes = []
            scores = []
            for pt in zip(*loc[::-1]):
                boxes.append([pt[0] + offset_x, pt[1] + offset_y, pt[0] + offset_x + tw, pt[1] + offset_y + th])
                scores.append(float(res[pt[1], pt[0]]))

            if not boxes:
                return []

            indices = cv2.dnn.NMSBoxes(
                bboxes=[[b[0], b[1], b[2] - b[0], b[3] - b[1]] for b in boxes],
                scores=scores,
                score_threshold=threshold,
                nms_threshold=0.4
            )

            results = []
            if len(indices) > 0:
                for idx in indices.flatten()[:max_results]:
                    b = boxes[idx]
                    score = scores[idx]
                    results.append(MatchResult(b[0], b[1], b[2] - b[0], b[3] - b[1], score, template_name))

            return results
        return []
