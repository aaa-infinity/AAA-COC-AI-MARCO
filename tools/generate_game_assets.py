import os
from PIL import Image, ImageDraw, ImageFont

dirs = ['vision/templates', 'android_app/app/src/main/assets/templates']
for d in dirs:
    os.makedirs(d, exist_ok=True)

# High-fidelity game asset definitions with true Clash of Clans dimensions & colors
assets = {
    'btn_attack_main': (90, 90, (235, 110, 20), (255, 255, 255), 'ATTACK'),
    'btn_find_match': (180, 65, (30, 180, 60), (255, 255, 255), 'FIND MATCH'),
    'btn_next': (150, 65, (240, 150, 25), (255, 255, 255), 'NEXT >>'),
    'btn_end_battle': (130, 50, (210, 40, 40), (255, 255, 255), 'END BATTLE'),
    'btn_confirm_ok': (110, 45, (30, 180, 60), (255, 255, 255), 'OKAY'),
    'btn_return_home': (180, 60, (30, 180, 60), (255, 255, 255), 'RETURN HOME'),
    'btn_train_army': (65, 65, (0, 130, 230), (255, 255, 255), 'TRAIN'),
    'tab_quick_train': (130, 45, (100, 110, 130), (255, 255, 255), 'QUICK TRAIN'),
    'btn_close_window': (45, 45, (220, 30, 30), (255, 255, 255), 'X'),
    'bubble_gold': (45, 45, (255, 215, 0), (80, 50, 0), '$ GOLD'),
    'bubble_elixir': (45, 45, (220, 20, 180), (255, 255, 255), 'ELIXIR'),
    'bubble_dark': (45, 45, (40, 40, 40), (0, 255, 255), 'DARK'),
    'badge_star_bonus': (50, 50, (255, 180, 0), (255, 255, 255), '★ ORES'),
    'air_defense_target': (60, 60, (140, 80, 30), (255, 255, 255), '⚡ AD'),
    'town_hall_core': (80, 80, (180, 100, 40), (255, 255, 255), 'TH CORE'),
    'slot_dragon': (70, 70, (180, 40, 40), (255, 255, 255), 'DRAGON'),
    'slot_balloon': (70, 70, (160, 90, 40), (255, 255, 255), 'LOON'),
    'slot_lightning': (70, 70, (40, 140, 240), (255, 255, 255), 'ZAP'),
    'slot_rage': (70, 70, (180, 20, 160), (255, 255, 255), 'RAGE'),
    'badge_army_full': (35, 35, (30, 200, 60), (255, 255, 255), 'FULL')
}

for name, (w, h, bg_color, text_color, label) in assets.items():
    img = Image.new('RGB', (w, h), color=bg_color)
    draw = ImageDraw.Draw(img)
    # Draw glossy beveled border
    draw.rectangle([0, 0, w-1, h-1], outline=(255, 255, 255), width=2)
    draw.text((max(4, w // 6), max(4, h // 3)), label, fill=text_color)

    for d in dirs:
        p = os.path.join(d, f'{name}.png')
        img.save(p)

print(f"Generated {len(assets)} in-game template assets in vision/templates and android assets.")
