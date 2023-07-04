# playlist-kotlin

A work in progress playlist module for Brave browser written in kotlin and can be integrated with
Kotlin or Java project.
This module includes UI and related behaviours for playlist feature.

### Build

To build .aar for module, please
follow [Build Script](https://github.com/brave-experiments/playlist-kotlin/blob/dp_playlist_brave_app_work/script/build_aar.sh)

### Sign .aar

jarsigner comes bundled in the JDK. As such, it is immediately available after installing it.
jarsigner needs a keystore containing a key pair for signing.

```
jarsigner -keystore KEYSTORE_FILEPATH -storepass KEYSTORE_PASSWORD -signedjar playlist_signed.aar -verbose playlist.aar KEYSTORE_ALIAS
```

### Verify .aar

```
jarsigner -keystore KEYSTORE_FILEPATH -storepass KEYSTORE_PASSWORD -verify -verbose -certs playlist_signed.aar KEYSTORE_ALIAS
```
