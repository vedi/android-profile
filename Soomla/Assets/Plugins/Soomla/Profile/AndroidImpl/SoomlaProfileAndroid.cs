﻿/// Copyright (C) 2012-2014 Soomla Inc.
///
/// Licensed under the Apache License, Version 2.0 (the "License");
/// you may not use this file except in compliance with the License.
/// You may obtain a copy of the License at
///
///      http://www.apache.org/licenses/LICENSE-2.0
///
/// Unless required by applicable law or agreed to in writing, software
/// distributed under the License is distributed on an "AS IS" BASIS,
/// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
/// See the License for the specific language governing permissions and
/// limitations under the License.

using UnityEngine;
using System;
using System.Runtime.InteropServices;

namespace Soomla.Profile {

	/// <summary>
	/// <c>SoomlaProfile</c> for Android. 
	/// This class holds the basic assets needed to operate the Profile module.
	/// 
	/// See comments for functions in parent.
	/// </summary>
	public class SoomlaProfileAndroid : SoomlaProfile {

#if UNITY_ANDROID

		protected override void _initialize() {

			AndroidJNI.PushLocalFrame(100);
			using(AndroidJavaClass jniSoomlaProfileClass = new AndroidJavaClass("com.soomla.profile.SoomlaProfile")) {
				AndroidJavaObject jniSoomlaProfile = jniSoomlaProfileClass.CallStatic<AndroidJavaObject>("getInstance");
				jniSoomlaProfile.Call("initialize", true);
			}
			AndroidJNI.PopLocalFrame(IntPtr.Zero);
		}

		protected override UserProfile _getStoredUserProfile(Provider provider) {
			JSONObject upObj = null;
			AndroidJNI.PushLocalFrame(100);
			using(AndroidJavaClass jniSoomlaProfile = new AndroidJavaClass("com.soomla.profile.unity.UnitySoomlaProfile")) {
				string upJSON = ProfileJNIHandler.CallStatic<string>(jniSoomlaProfile, "getStoredUserProfile", provider.ToString());
				if(upJSON != null) {
					upObj = new JSONObject(upJSON);
				}
			}
			AndroidJNI.PopLocalFrame(IntPtr.Zero);

			if (upObj) {
				return new UserProfile(upObj);
			} else {
				return null;
			}
		}

		protected override void _storeUserProfile(UserProfile userProfile, bool notify) {
			AndroidJNI.PushLocalFrame(100);
			using(AndroidJavaClass jniSoomlaProfile = new AndroidJavaClass("com.soomla.profile.unity.UnitySoomlaProfile")) {
				ProfileJNIHandler.CallStaticVoid(jniSoomlaProfile, "storeUserProfile",
				                                 userProfile.toJSONObject().ToString());
			}
			AndroidJNI.PopLocalFrame(IntPtr.Zero);
		}

		protected override void _openAppRatingPage() {
			AndroidJNI.PushLocalFrame(100);
			using (AndroidJavaClass unityActivityClass = new AndroidJavaClass("com.unity3d.player.UnityPlayer")) {
				using(AndroidJavaObject unityActivity = unityActivityClass.GetStatic<AndroidJavaObject>("currentActivity")) {
					using(AndroidJavaClass jniSoomlaProfile = new AndroidJavaClass("com.soomla.profile.unity.UnitySoomlaProfile")) {
						ProfileJNIHandler.CallStaticVoid(jniSoomlaProfile, "openAppRatingPage", unityActivity);
					}
				}
			}

			AndroidJNI.PopLocalFrame(IntPtr.Zero);
		}

#endif
	}
}