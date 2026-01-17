from PIL import Image
import os

# 入力画像のパス
input_path = r"C:\Users\seizo\Desktop\App\temporary-memo\screenshots\S__22053091_0.jpg"

# 画像を開く
img = Image.open(input_path)
img = img.convert("RGBA")

# 白地を透明に変換
datas = img.getdata()
new_data = []

for item in datas:
    # 白っぽい色を透明にする（RGB値が240以上を白とみなす）
    if item[0] > 240 and item[1] > 240 and item[2] > 240:
        new_data.append((255, 255, 255, 0))  # 透明にする
    else:
        new_data.append(item)

img.putdata(new_data)

# 出力ディレクトリの作成
output_dirs = {
    "mdpi": 48,
    "hdpi": 72,
    "xhdpi": 96,
    "xxhdpi": 144,
    "xxxhdpi": 192
}

# mipmapディレクトリのベースパス
base_path = r"C:\Users\seizo\Desktop\App\temporary-memo\app\src\main\res"

# 各サイズのアイコンを生成
for density, size in output_dirs.items():
    dir_path = os.path.join(base_path, f"mipmap-{density}")
    os.makedirs(dir_path, exist_ok=True)

    # アンチエイリアスを使って高品質にリサイズ
    resized = img.resize((size, size), Image.Resampling.LANCZOS)

    # ic_launcher.png として保存
    output_path = os.path.join(dir_path, "ic_launcher.png")
    resized.save(output_path, "PNG")
    print(f"Created: {output_path}")

# Play Store用の512x512pxアイコンも生成
store_listing_path = r"C:\Users\seizo\Desktop\App\temporary-memo\store_listing"
os.makedirs(store_listing_path, exist_ok=True)

resized_512 = img.resize((512, 512), Image.Resampling.LANCZOS)
output_512 = os.path.join(store_listing_path, "app_icon_512.png")
resized_512.save(output_512, "PNG")
print(f"Created: {output_512}")

print("\n✅ All icon files created successfully!")
print("\nGenerated icons:")
print("- mipmap-mdpi/ic_launcher.png (48x48)")
print("- mipmap-hdpi/ic_launcher.png (72x72)")
print("- mipmap-xhdpi/ic_launcher.png (96x96)")
print("- mipmap-xxhdpi/ic_launcher.png (144x144)")
print("- mipmap-xxxhdpi/ic_launcher.png (192x192)")
print("- store_listing/app_icon_512.png (512x512)")
