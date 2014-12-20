### v1.0.2 [view commit logs](https://github.com/soomla/android-profile/compare/v1.0.1...v1.0.2)

* Fixes
  * If login success listener has an exception, we fail gracefully and not letting onException take it. This is how we prevent LoginFailedEvent right after LoginFinishedEvent.

### v1.0.1 [view commit logs](https://github.com/soomla/android-profile/compare/v1.0.0...v1.0.1)
* Changes
  * Updated Core submodule

### v1.0.0 (16.11.14)
* Features
  * The module is integrated with Facebook, Google Plus and Twitter
  * Ability to preform following actions on multiple social networks (parallel):
    * Login/Logout
    * Update status
    * Update Story (supported fully in Facebook only)
    * Upload image
    * Get user profile + store it on the device
    * Get user's contacts (not all social networks provide all information)
    * Get user's most recent feed (not supported in Google Plus)
