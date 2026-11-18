BOX = "M36,34H72A10,10 0 0 1 82,44V64A10,10 0 0 1 72,74H36A10,10 0 0 1 26,64V44A10,10 0 0 1 36,34Z"
WHITE = [
 "M46.5,42H61.5Q63,42 63.6,43.4L66.5,51H41.5L44.4,43.4Q45,42 46.5,42Z",
 "M42,50.5H66Q68,50.5 68,52.5V62Q68,63.5 66.5,63.5H41.5Q40,63.5 40,62V52.5Q40,50.5 42,50.5Z",
 "M41.5,63H46.5V66Q46.5,67.5 45,67.5H43Q41.5,67.5 41.5,66Z",
 "M61.5,63H66.5V66Q66.5,67.5 65,67.5H63Q61.5,67.5 61.5,66Z",
 "M38,48.5H42.5V51H38.8Q38,51 38,50.2Z",
 "M65.5,48.5H70V50.2Q70,51 69.2,51H65.5Z",
]
RED = [
 "M47.6,44H60.4L63,50H45Z",
 "M42.8,56A2.2,2.2 0 1 0 47.2,56A2.2,2.2 0 1 0 42.8,56Z",
 "M60.8,56A2.2,2.2 0 1 0 65.2,56A2.2,2.2 0 1 0 60.8,56Z",
 "M50,58.5H58V60.5H50Z",
]
R="#FF0000"; W="#FFFFFF"
def body(fmt):
    out=[fmt(BOX,R)]+[fmt(p,W) for p in WHITE]+[fmt(p,R) for p in RED]
    return "\n".join(out)
xml = '<?xml version="1.0" encoding="utf-8"?>\n<!-- Ikona TubeAUTO: czerwony prostokąt jak YouTube, w środku auto od przodu (plik generowany przez ikona/paths.py) -->\n<vector xmlns:android="http://schemas.android.com/apk/res/android"\n    android:width="108dp" android:height="108dp"\n    android:viewportWidth="108" android:viewportHeight="108">\n' + body(lambda d,c: f'    <path android:fillColor="{c}" android:pathData="{d}" />') + '\n</vector>\n'
open(r"C:\TEMP\TubeAUTO\app\src\main\res\drawable\ic_launcher_foreground.xml","w",encoding="utf-8").write(xml)
open(r"C:\TEMP\TubeAUTO\app\src\main\res\drawable\ic_launcher_background.xml","w",encoding="utf-8").write('<?xml version="1.0" encoding="utf-8"?>\n<shape xmlns:android="http://schemas.android.com/apk/res/android" android:shape="rectangle">\n    <solid android:color="#FFFFFF" />\n</shape>\n')
open(r"C:\TEMP\TubeAUTO\app\src\main\res\mipmap-anydpi\ic_launcher.xml","w",encoding="utf-8").write('<?xml version="1.0" encoding="utf-8"?>\n<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">\n    <background android:drawable="@drawable/ic_launcher_background" />\n    <foreground android:drawable="@drawable/ic_launcher_foreground" />\n</adaptive-icon>\n')
svg = '<html><body style="margin:0;background:#222"><svg width="432" height="432" viewBox="18 18 72 72"><clipPath id="c"><rect x="18" y="18" width="72" height="72" rx="20"/></clipPath><g clip-path="url(#c)"><rect width="108" height="108" fill="#fff"/>' + body(lambda d,c: f'<path fill="{c}" d="{d}"/>') + '</g></svg></body></html>'
open(r"C:\TEMP\TubeAUTO\ikona\podglad.html","w",encoding="utf-8").write(svg)
