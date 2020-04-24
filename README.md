# DMTQ Localserver

An Android local server for DJMAX Technika Q

Work both with Android (Modified game client or proxy mode) and iOS (Proxy mode)

## File structure

The server will read file from `/sdcard/Android/data/icu.samnya.dmtq_server/files`

For this app to run, you need at least these file or folder inside:

`/patch` folder : Latest update pack for the game, the game load it every time at boot to check game version.

`/Patterns` folder or `Patterns.zip` file : The game patterns file.

    This game download patterns file from server every time you start a song.

    The file name inside should contains only patternId, or _EARPHONE suffix. eg: `1` `1_1_EARPHONE` ...

`/Songs` folder : For music and movie file.

    In `Asset Server` setting, if you use localhost then it will load from here.

    Or you can host those file on a external server.

    File name must follow musicId.fpk musicId.webm eg: `1.fpk` `1.webm`

`/static` folder : For banner or ads, can be empty.

`/viewImg` folder : The eye folder inside is for music jacket, if you have nothing the game will fail to download songs.

    File name must use the `name` field in `songList.json`. eg: `oblivion.png`

`ca.crt` file : The SSL cert for iOS device. Android doesn't need this if you modify the client to connect localhost with http.

`keystore.jks` file : The SSL keystore file for HTTPS server.

`songList.json` file : The song list info. If you add custom patterns, add the song info here.

## Running with proxy mode

For iOS device or Android device with a unmodified client, you need to use proxy mode to run the game.

1. First set `HTTP Server Address` and `Proxy Server Address` to server's IP:PORT

2. Start the server.

3. Then use a web browser (Use Safari on iOS) to access http://IP:PORT you just set.

4. Download the ca.crt file, follow the instruction to install the certificate and trust it.

5. Set your device http proxy setting in WiFi setting. You can use either Auto or Manual mode.

    For Auto mode, go to the server's web page, copy the url of the PAC file, then paste it to the http proxy setting on your device.

    For Manual mode, just type the IP and Port you have set.

    (I suggest Auto mode since it won't slow down other network connection)

6. You should be able to run the game.

You may need to check your background running setting for your android device, since some Android device is very strict to background app, even switch to another app will cause the server go to sleep.
