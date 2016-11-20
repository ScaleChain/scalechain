## Steps for preparing porting
### Change file extentions from scala to kt
```
find . -name "*.scala" -exec bash -c 'mv "$1" "${1%.scala}".kt' - '{}' \;
```

