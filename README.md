This Android app collects Mag-Stripe data and CVC3 codes from MasterCard PayPass cards and emulates that informations.
It is based on combined pre-play and downgrade attack described in "Cloning Credit Cards: A combined pre-play and downgrade attack on EMV Contactless" by Michael Roland, Josef Langer.

**WARNING!** This application might destroy your credit card (MasterCard only) after ~ 66 successful attacks.*

*For each attack application increments card's ATC by 1000. ATC (Application Transaction Counter) is 2B value that means that maximum value of ATC is 65535, so after approximately 66 attacks this counter overflows.
