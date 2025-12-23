# Android Minimalist Launcher

<img src="./static/logo.svg" alt="logo of the launcher" width="100"/>



A distraction-free Android launcher focused on simplicity and essential interactions. Built entirely using [github/spec-kit](https://github.com/github/spec-kit) to explore spec-driven development with Android/Kotlin.

<img src="./static/screenshot.png" alt="screenshot-of-the-launcher" width="300"/>

## Features

- **Clean Home Screen**: Displays only time, date, and favorite apps
- **Smart Battery Indicator**: Circular indicator around camera notch (Pixel 8 Pro) that appears only when battery is below 50%
- **Usage Awareness**: Mindful phone use features without judgment or control
  - **Unlock Counter**: See how many times you've unlocked your phone today in the top-left corner
  - **App Launch Awareness**: Brief overlay shows "8th time today" when opening apps
  - **Last Launch Time**: See "opened 20m ago" to notice compulsive checking patterns
  - All data resets at midnight and stays local - no cloud sync, no tracking
- **Gesture Navigation**: 
  - Swipe right-to-left for app search
  - Swipe left-to-right in search to return to home (with smooth animation)
  - Swipe up for device search
  - Swipe down for notifications
  - Search UX: keyboard automatically hides when you start scrolling results
- **Favorite Apps Management**: Long-press apps in search to add/remove from home screen
- **Quick Actions**: Bottom corner shortcuts for phone and camera apps with press animations
- **Clock Quick Access**: Tap time/date display to instantly open your alarm/clock app
- **Smooth Animations**: Modern micro-interactions and 60fps transitions throughout
- **Distraction-Free**: No widgets, no clutter, just what you need

> **Note**: This launcher is part of a personal journey to reduce phone distractions and spend less time mindlessly scrolling. By removing visual clutter and making apps deliberately accessible through search, it encourages more intentional phone usage.

## Design Philosophy

**"Make unconscious behavior visible, then step aside."**

This launcher exists to make phone use **conscious, not controlled**. It introduces small moments of awareness before action, trusting users to decide for themselves.

### Core Principles

1. **Awareness, Not Guilt**  
   Usage data is presented minimally and factually, never with judgment or shame-inducing language. The goal is to illuminate patterns, not punish behavior.

2. **Consciousness Without Invasive Permissions**  
   We work within normal launcher capabilities—no drawing over apps, no accessibility service abuse, no screen content tracking. Awareness doesn't require surveillance.

3. **Autonomy Over Control**  
   This launcher doesn't block apps, manipulate emotions, or optimize for engagement. It pauses the autopilot with gentle nudges (like showing unlock counts or app launch frequency), but never restricts or nags.

4. **Clarity, Calm, and Respect**  
   No guilt, no punishment, no dark patterns—just clear information that helps users make informed choices about their attention, if they want to.

## Development Journey

The goal of this project was to experience spec driven development with:
* An eco-system I haven't coded for in 10 years (Android);
* A programming language I have never tried (Kotlin);
* A stack I've never been profecient with (Mobile);

## CI/CD

This project uses GitHub Actions for automated releases to the Google Play Store.

- **Automated Releases**: Publish a GitHub Release to trigger automatic Play Store deployment
- **PR Validation**: All pull requests are automatically built and tested
- **Setup Guide**: See [.github/RELEASE_SETUP.md](.github/RELEASE_SETUP.md) for configuration instructions
- **Quick Release**: See [RELEASE.md](RELEASE.md) for the release process

## Future features

* Have whatsapp on the bottom right instead of camera? Maybe a setting for this?
* **Settings Menu**: Toggle auto-launch and other preferences
* **Dynamic Backgrounds**: Automatic background generation (exploring generative art)
