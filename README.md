# eve-corp-ratting-taxes

Tool to aggregate ratting taxes per day.

## Build

`mvn clean package`

## Run

`SECRET=mySecret:string CLIENT_ID=clientId:string CLIENT_SECRET=clientSecret:string REFRESH_TOKEN=refreshToken:string CORPORATION_ID=corporationId:long WALLET_DIVISION=walletDivision:long java -jar corp-ratting-taxes-VERSION.jar`
