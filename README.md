#EMVemulator#
This Android app collects Mag-Stripe data and CVC3 codes from MasterCard PayPass cards and emulates that information.
It is based on combined pre-play and downgrade attack described in [Cloning Credit Cards: A combined pre-play and downgrade attack on EMV Contactless](https://github.com/MatusKysel/EMVemulator/raw/master/Cloning%20Credit%20Cards%20A%20combined%20pre-play-Roland.pdf) by Michael Roland, Josef Langer.

**WARNING!** This application might destroy your credit card (MasterCard only) after ~ 66 successful attacks.*

*For each attack application increments card's ATC by 1000. ATC (Application Transaction Counter) is 2B value that means that maximum value of ATC is 65535, so after approximately 66 attacks this counter overflows.

##Downloads##
[Source code](https://github.com/MatusKysel/EMVemulator/archive/v1.1.zip)

[APK](https://github.com/MatusKysel/EMVemulator/releases/download/v1.1/EMVemulator.apk)

##Usage##
This is just proof of concept application, so interface is quite simple.

1, This screen shows, that app is ready for reading card

![alt text](https://github.com/MatusKysel/EMVemulator/raw/master/pic/Readermode.png "App is ready")

2, When you have compatible card (MasterCard PayPass) app will start collecting data

![alt text](https://github.com/MatusKysel/EMVemulator/raw/master/pic/Reading.png "Collecting data")

3, Data collection is completed and your phone is ready for emulating your card

![alt text](https://github.com/MatusKysel/EMVemulator/raw/master/pic/Completed.png "Finished")

4, Now you can put this app to background or just close it and whenever you want to reply collected data just turn on your screen and app will start to communicate with reader
