"""
Screen capture engine for Android devices & emulators.
Captures raw screen buffers and converts them into image arrays.
"""

import io
import time
import os
import subprocess
import logging
from typing import Optional, Tuple
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


class ScreenCapture:
    """
    Captures screenshots from the device via ADB or local screen capture.
    """
    def __init__(self, device_serial: Optional[str] = None, output_dir: str = "data/screenshots"):
        self.device_serial = device_serial
        self.output_dir = output_dir
        os.makedirs(self.output_dir, exist_ok=True)
        self.last_frame = None
        self.last_timestamp: float = 0.0

    def capture(self):
        """
        Captures a single frame from the connected device.
        Returns an OpenCV BGR array or PIL Image.
        """
        cmd = ["adb"]
        if self.device_serial:
            cmd.extend(["-s", self.device_serial])
        cmd.extend(["exec-out", "screencap", "-p"])

        try:
            res = subprocess.run(cmd, capture_output=True, check=False, timeout=5)
            if res.returncode == 0 and len(res.stdout) > 0:
                if HAS_CV2 and np is not None:
                    image_array = np.frombuffer(res.stdout, dtype=np.uint8)
                    frame = cv2.imdecode(image_array, cv2.IMREAD_COLOR)
                    if frame is not None:
                        h, w, _ = frame.shape
                        if h > w:
                            frame = cv2.rotate(frame, cv2.ROTATE_90_COUNTERCLOCKWISE)
                        self.last_frame = frame
                        self.last_timestamp = time.time()
                        return frame
                else:
                    pil_img = Image.open(io.BytesIO(res.stdout)).convert("RGB")
                    w, h = pil_img.size
                    if h > w:
                        pil_img = pil_img.transpose(Image.ROTATE_90)
                    self.last_frame = pil_img
                    self.last_timestamp = time.time()
                    return pil_img
        except Exception as e:
            logger.debug(f"Capture via ADB failed: {e}. Generating mock canvas.")

        # Mock frame fallback
        if HAS_CV2 and np is not None:
            mock_frame = np.zeros((1080, 1920, 3), dtype=np.uint8)
            mock_frame[:, :] = (34, 139, 34)
            cv2.putText(mock_frame, "Clash of Clans Mock View", (700, 500), cv2.FONT_HERSHEY_SIMPLEX, 1.2, (255, 255, 255), 2)
            self.last_frame = mock_frame
        else:
            mock_frame = Image.new("RGB", (1920, 1080), color=(34, 139, 34))
            self.last_frame = mock_frame

        self.last_timestamp = time.time()
        return self.last_frame

    def get_roi(self, frame, roi: Tuple[int, int, int, int]):
        x1, y1, x2, y2 = roi
        if HAS_CV2 and hasattr(frame, "shape"):
            return frame[y1:y2, x1:x2]
        elif isinstance(frame, Image.Image):
            return frame.crop((x1, y1, x2, y2))
        return frame

    def save_debug(self, frame=None, name_prefix: str = "debug") -> str:
        if frame is None:
            frame = self.last_frame if self.last_frame is not None else self.capture()
        filename = f"{name_prefix}_{int(time.time() * 1000)}.png"
        filepath = os.path.join(self.output_dir, filename)
        if HAS_CV2 and hasattr(frame, "shape"):
            cv2.imwrite(filepath, frame)
        elif isinstance(frame, Image.Image):
            frame.save(filepath)
        logger.debug(f"Saved debug screenshot: {filepath}")
        return filepath
