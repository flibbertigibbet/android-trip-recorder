# android-trip-recorder

Using MapBox libraries to record and replay trips to test navigation

## Requirements

 - Android Studio
 - Java 8
 - Kotlin
 - a MapBox account


## Setup

Set up a MapBox account. Create a secret access token, as described [here](https://docs.mapbox.com/android/beta/navigation/overview/#configure-credentials).

Copy the example API keys file for the client into the app:

`cp examples/api_keys.xml app/src/main/res/values/`

Put the public token for your MapBox account into `app/src/main/res/values/api_keys.xml` as the value for the `mapbox_access_token` key.

Create a local `gradle.properties` file in your user directory and add your secret MapBox token to it as the value for `MAPBOX_DOWNLOADS_TOKEN`, as described [here](https://docs.mapbox.com/android/beta/navigation/overview/#configure-your-secret-token).
