# Android Player SDK Setup Guide

## Prerequisites

- Android Studio (latest version recommended)
- Java 17 (minimum required)
- Gradle 8.7
- Android Gradle Plugin (AGP) 7.3.1
- Kotlin 1.7.10
- Compile SDK: 34 (app module), 33 (player module)
- Target SDK: 34 (app module), 33 (player module)
- Min SDK: 21

## Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/testpress/android-player-sdk.git
cd android-player-sdk
```

### 2. Open in Android Studio

1. Launch Android Studio
2. Select "Open an existing project"
3. Navigate to the cloned repository folder
4. Click "OK" to import the project

### 3. Build the Project

1. Wait for Gradle sync to complete
2. Build the project using `Build > Make Project` or press `Ctrl+F9` (Windows/Linux) or `Cmd+F9` (Mac)

### 4. Run the Sample App

1. Select the `app` module from the run configuration dropdown
2. Click the Run button or press `Shift+F10`

## Project Structure

- `app/` - Sample application demonstrating SDK usage
- `player/` - Core SDK module containing the player implementation
- `docs/` - Documentation files

## Integration

To integrate the SDK into your own project:

1. Add the `player` module as a dependency in your `build.gradle`:
```gradle
implementation project(':player')
```

2. Add required permissions to your `AndroidManifest.xml`:
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

3. Initialize the player in your activity or fragment following the examples in the sample app.

## Troubleshooting

- Ensure your project's `compileSdkVersion` and `targetSdkVersion` are set to API level 33 or higher
- If you encounter build issues, try cleaning and rebuilding the project
- Check that all required dependencies are properly resolved
- Verify that your Gradle and AGP versions match the requirements above
- **Gradle Version**: The project uses Gradle 8.7 by default, which requires Java 17 or higher to run.
- Check your JAVA_HOME environment variable points to a JDK 17+ installation.