from pathlib import Path
import zipfile,sys
root=Path(__file__).resolve().parent/'root'
output=Path(sys.argv[1]); output.parent.mkdir(parents=True,exist_ok=True)
apk=Path(sys.argv[2]); assert apk.is_file() and zipfile.is_zipfile(apk)
with zipfile.ZipFile(output,'w',zipfile.ZIP_DEFLATED) as z:
    for f in sorted(root.rglob('*')):
        if f.is_file(): z.write(f,f.relative_to(root))
    z.write(apk,'companion.apk')
with zipfile.ZipFile(output) as z:
    assert z.testzip() is None
    assert 'module.prop' in z.namelist() and 'companion.apk' in z.namelist()
print(output)
