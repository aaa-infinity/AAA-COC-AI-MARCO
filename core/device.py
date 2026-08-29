"""
Android Device Controller via ADB with Humanized Input Emulation.
Supports touch jitter, Bezier curve swipes, and emulator connection discovery.
"""

import random
import time
import subprocess
import logging
from typing import Tuple, List, Optional

logger = logging.getLogger(__name__)


class DeviceController:
    """
    Controls an Android device or emulator via ADB shell commands.
    """
    def __init__(
        self,
        serial: Optional[str] = None,
        adb_host: str = "127.0.0.1",
        adb_port: int = 5555,
        ref_resolution: Tuple[int, int] = (1920, 1080),
        enable_jitter: bool = True,
        jitter_std: float = 3.0
    ):
        self.serial = serial
        self.adb_host = adb_host
        self.adb_port = adb_port
        self.ref_w, self.ref_h = ref_resolution
        self.actual_w, self.actual_h = ref_resolution
        self.enable_jitter = enable_jitter
        self.jitter_std = jitter_std
        self.connected = False
        self._detect_and_connect()

    def _adb_cmd(self, args: List[str]) -> subprocess.CompletedProcess:
        base_cmd = ["adb"]
        if self.serial:
            base_cmd.extend(["-s", self.serial])
        full_cmd = base_cmd + args
        try:
            res = subprocess.run(
                full_cmd,
                capture_output=True,
                text=True,
                check=False,
                timeout=10
            )
            return res
        except subprocess.TimeoutExpired:
            logger.error(f"ADB command timed out: {' '.join(full_cmd)}")
            return subprocess.CompletedProcess(args=full_cmd, returncode=-1, stdout="", stderr="Timeout")
        except Exception as e:
            logger.error(f"ADB command failed: {e}")
            return subprocess.CompletedProcess(args=full_cmd, returncode=-1, stdout="", stderr=str(e))

    def _detect_and_connect(self):
        common_ports = [5555, 5554, 62001, 21503, 7555]
        subprocess.run(["adb", "start-server"], capture_output=True, text=True)

        res = subprocess.run(["adb", "devices"], capture_output=True, text=True)
        devices = []
        for line in res.stdout.strip().splitlines()[1:]:
            parts = line.split()
            if len(parts) >= 2 and parts[1] == "device":
                devices.append(parts[0])

        if not devices and not self.serial:
            logger.info("No device detected. Scanning standard emulator ports...")
            for port in common_ports:
                addr = f"{self.adb_host}:{port}"
                subprocess.run(["adb", "connect", addr], capture_output=True, text=True, timeout=2)
            
            res = subprocess.run(["adb", "devices"], capture_output=True, text=True)
            for line in res.stdout.strip().splitlines()[1:]:
                parts = line.split()
                if len(parts) >= 2 and parts[1] == "device":
                    devices.append(parts[0])

        if self.serial and self.serial in devices:
            self.connected = True
            logger.info(f"Connected to device with serial: {self.serial}")
        elif devices:
            self.serial = devices[0]
            self.connected = True
            logger.info(f"Auto-selected first connected device: {self.serial}")
        else:
            logger.warning("No Android device or emulator found. Running in MOCK/SIMULATION mode.")
            self.connected = False

        if self.connected:
            self._fetch_device_resolution()

    def _fetch_device_resolution(self):
        res = self._adb_cmd(["shell", "wm", "size"])
        if "Physical size:" in res.stdout:
            for line in res.stdout.splitlines():
                if "Physical size:" in line:
                    dims = line.split(":")[-1].strip().split("x")
                    if len(dims) == 2:
                        self.actual_w = int(dims[0])
                        self.actual_h = int(dims[1])
                        if self.actual_w < self.actual_h:
                            self.actual_w, self.actual_h = self.actual_h, self.actual_w
                        logger.info(f"Device Resolution: {self.actual_w}x{self.actual_h}")

    def scale_coords(self, x: int, y: int) -> Tuple[int, int]:
        scale_x = self.actual_w / float(self.ref_w)
        scale_y = self.actual_h / float(self.ref_h)
        return int(x * scale_x), int(y * scale_y)

    def tap(
        self,
        x: int,
        y: int,
        jitter: bool = True,
        delay_range: Tuple[float, float] = (0.3, 0.7)
    ):
        if jitter and self.enable_jitter:
            x += int(random.gauss(0, self.jitter_std))
            y += int(random.gauss(0, self.jitter_std))

        x = max(5, min(self.actual_w - 5, x))
        y = max(5, min(self.actual_h - 5, y))

        if self.connected:
            self._adb_cmd(["shell", "input", "tap", str(x), str(y)])
        else:
            logger.debug(f"[MOCK TAP] at ({x}, {y})")

        sleep_time = random.uniform(*delay_range)
        time.sleep(sleep_time)

    def tap_region(
        self,
        x1: int,
        y1: int,
        x2: int,
        y2: int,
        delay_range: Tuple[float, float] = (0.3, 0.7)
    ):
        target_x = random.randint(min(x1, x2), max(x1, x2))
        target_y = random.randint(min(y1, y2), max(y1, y2))
        self.tap(target_x, target_y, jitter=False, delay_range=delay_range)

    def swipe(
        self,
        start: Tuple[int, int],
        end: Tuple[int, int],
        duration_ms: int = 300,
        natural_curve: bool = True
    ):
        x1, y1 = start
        x2, y2 = end

        if not natural_curve or not self.connected:
            if self.connected:
                self._adb_cmd(["shell", "input", "swipe", str(x1), str(y1), str(x2), str(y2), str(duration_ms)])
            else:
                logger.debug(f"[MOCK SWIPE] from {start} to {end} in {duration_ms}ms")
            time.sleep(duration_ms / 1000.0 + random.uniform(0.1, 0.3))
            return

        varied_duration = int(duration_ms * random.uniform(0.9, 1.15))
        self._adb_cmd([
            "shell", "input", "swipe",
            str(x1), str(y1), str(x2), str(y2),
            str(varied_duration)
        ])
        time.sleep(varied_duration / 1000.0 + random.uniform(0.1, 0.3))

    def pinch_zoom(self, zoom_in: bool = False, steps: int = 10):
        center_x = self.actual_w // 2
        center_y = self.actual_h // 2
        offset = 250

        if zoom_in:
            self.swipe((center_x - 50, center_y), (center_x - offset, center_y), duration_ms=250)
        else:
            self.swipe((center_x - offset, center_y), (center_x - 50, center_y), duration_ms=250)

    def press_back(self):
        if self.connected:
            self._adb_cmd(["shell", "input", "keyevent", "4"])
        time.sleep(random.uniform(0.3, 0.6))
