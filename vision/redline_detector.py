"""
Red Deployment Boundary Detector.
Detects enemy base no-spawn zones (red lines) and computes optimal troop drop points.
"""

import logging
from typing import List, Tuple, Optional
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


class RedlineDetector:
    """
    Detects the red boundary polygon and calculates spawn coordinates outside the red zone.
    """
    def __init__(self):
        if HAS_CV2 and np is not None:
            self.lower_red1 = np.array([0, 150, 150])
            self.upper_red1 = np.array([10, 255, 255])
            self.lower_red2 = np.array([170, 150, 150])
            self.upper_red2 = np.array([180, 255, 255])

    def find_redline_mask(self, screen):
        if HAS_CV2 and hasattr(screen, "shape") and np is not None:
            hsv = cv2.cvtColor(screen, cv2.COLOR_BGR2HSV)
            mask1 = cv2.inRange(hsv, self.lower_red1, self.upper_red1)
            mask2 = cv2.inRange(hsv, self.lower_red2, self.upper_red2)
            red_mask = cv2.bitwise_or(mask1, mask2)
            kernel = cv2.getStructuringElement(cv2.MORPH_ELLIPSE, (5, 5))
            closed = cv2.morphologyEx(red_mask, cv2.MORPH_CLOSE, kernel)
            return closed
        return None

    def get_perimeter_deployment_points(
        self,
        screen_shape: Tuple[int, int],
        side: str = "ALL",
        points_per_side: int = 6,
        margin: int = 80
    ) -> List[Tuple[int, int]]:
        h, w = screen_shape[:2]
        tl_corner = (margin, int(h * 0.45))
        tr_corner = (int(w * 0.5), margin)
        bl_corner = (int(w * 0.5), h - margin - 150)
        br_corner = (w - margin, int(h * 0.45))

        points = []

        if side in ("ALL", "TOP_LEFT"):
            for i in range(points_per_side):
                alpha = (i + 1) / float(points_per_side + 1)
                x = int(tl_corner[0] + alpha * (tr_corner[0] - tl_corner[0]))
                y = int(tl_corner[1] + alpha * (tr_corner[1] - tl_corner[1]))
                points.append((x, y))

        if side in ("ALL", "TOP_RIGHT"):
            for i in range(points_per_side):
                alpha = (i + 1) / float(points_per_side + 1)
                x = int(tr_corner[0] + alpha * (br_corner[0] - tr_corner[0]))
                y = int(tr_corner[1] + alpha * (br_corner[1] - tr_corner[1]))
                points.append((x, y))

        if side in ("ALL", "BOTTOM_RIGHT"):
            for i in range(points_per_side):
                alpha = (i + 1) / float(points_per_side + 1)
                x = int(br_corner[0] + alpha * (bl_corner[0] - br_corner[0]))
                y = int(br_corner[1] + alpha * (bl_corner[1] - br_corner[1]))
                points.append((x, y))

        if side in ("ALL", "BOTTOM_LEFT"):
            for i in range(points_per_side):
                alpha = (i + 1) / float(points_per_side + 1)
                x = int(bl_corner[0] + alpha * (tl_corner[0] - bl_corner[0]))
                y = int(bl_corner[1] + alpha * (tl_corner[1] - bl_corner[1]))
                points.append((x, y))

        return points
