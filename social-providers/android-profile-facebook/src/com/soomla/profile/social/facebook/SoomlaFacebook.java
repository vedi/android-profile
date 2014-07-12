package com.soomla.profile.social.facebook;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.soomla.profile.auth.AuthCallbacks;
import com.soomla.profile.domain.UserProfile;
import com.soomla.profile.social.ISocialProvider;
import com.soomla.profile.social.SocialCallbacks;
import com.sromku.simple.fb.Permission;
import com.sromku.simple.fb.SimpleFacebook;
import com.sromku.simple.fb.SimpleFacebookConfiguration;
import com.sromku.simple.fb.entities.Feed;
import com.sromku.simple.fb.entities.Photo;
import com.sromku.simple.fb.entities.Post;
import com.sromku.simple.fb.entities.Profile;
import com.sromku.simple.fb.entities.Story;
import com.sromku.simple.fb.listeners.OnFriendsListener;
import com.sromku.simple.fb.listeners.OnLoginListener;
import com.sromku.simple.fb.listeners.OnLogoutListener;
import com.sromku.simple.fb.listeners.OnPostsListener;
import com.sromku.simple.fb.listeners.OnProfileListener;
import com.sromku.simple.fb.listeners.OnPublishListener;

import java.util.ArrayList;
import java.util.List;

/**
 * @author vedi
 *         date 7/8/14
 *         time 6:48 PM
 */
public class SoomlaFacebook implements ISocialProvider {

    private SimpleFacebook mSimpleFacebook;

    public SoomlaFacebook() {

        Permission[] permissions = new Permission[] {
                Permission.USER_PHOTOS,
                Permission.EMAIL,
                Permission.PUBLISH_ACTION
        };

        SimpleFacebookConfiguration configuration = new SimpleFacebookConfiguration.Builder()
                .setAppId("409611972431183")
                .setNamespace("test")
                .setPermissions(permissions)
                .build();

        SimpleFacebook.setConfiguration(configuration);
    }

    @Override
    public void login(Activity activity, final AuthCallbacks.LoginListener loginListener) {
        mSimpleFacebook = SimpleFacebook.getInstance(activity);
        mSimpleFacebook.login(new OnLoginListener() {
            @Override
            public void onLogin() {
                loginListener.success(getProvider());
            }

            @Override
            public void onNotAcceptingPermissions(Permission.Type type) {
                loginListener.fail("onNotAcceptingPermissions: " + type);
            }

            @Override
            public void onThinking() {

            }

            @Override
            public void onException(Throwable throwable) {
                loginListener.fail("onException: " + throwable.getLocalizedMessage());
            }

            @Override
            public void onFail(String s) {
                loginListener.fail("onFail: " + s);
            }
        });
    }

    @Override
    public void logout(final AuthCallbacks.LogoutListener logoutListener) {
        if (mSimpleFacebook != null) {
            mSimpleFacebook.logout(new OnLogoutListener() {
                @Override
                public void onLogout() {
                    logoutListener.success();
                }

                @Override
                public void onThinking() {

                }

                @Override
                public void onException(Throwable throwable) {
                    logoutListener.fail("onException: " + throwable.getLocalizedMessage());
                }

                @Override
                public void onFail(String s) {
                    logoutListener.fail("onFail: " + s);
                }
            });
        }
    }

    @Override
    public void getUserProfile(final AuthCallbacks.UserProfileListener userProfileListener) {
        if (mSimpleFacebook != null) {

            Profile.Properties properties = new Profile.Properties.Builder()
                    .add(Profile.Properties.ID)
//                    .add(Profile.Properties.USER_NAME) //deprecated in v2
                    .add(Profile.Properties.NAME)
                    .add(Profile.Properties.EMAIL)
                    .add(Profile.Properties.FIRST_NAME)
                    .add(Profile.Properties.LAST_NAME)
                    .add(Profile.Properties.PICTURE)
                    .build();
            mSimpleFacebook.getProfile(properties, new OnProfileListener() {
                @Override
                public void onComplete(Profile response) {
                    super.onComplete(response);
                    final UserProfile userProfile = new UserProfile(getProvider(),
                            response.getId(), response.getName(), response.getEmail(),
                            response.getFirstName(), response.getLastName());
                    userProfile.setAvatarLink(response.getPicture());
                    // todo: verify extra permissions for these
//                    userProfile.setBirthday(response.getBirthday());
//                    userProfile.setGender(response.getGender());
//                    userProfile.setLanguage(response.getLanguages().get(0).getName());
//                    userProfile.setLocation(response.getLocation().getName());
                    userProfileListener.success(userProfile);
                }

                @Override
                public void onException(Throwable throwable) {
                    super.onException(throwable);
                    userProfileListener.fail("onException: " + throwable.getLocalizedMessage());
                }

                @Override
                public void onFail(String reason) {
                    super.onFail(reason);
                    userProfileListener.fail("onFail: " + reason);
                }
            });
        }
    }

    @Override
    public void updateStatus(String status, final SocialCallbacks.SocialActionListener socialActionListener) {
        Feed feed = new Feed.Builder()
                .setMessage(status)
                .build();

        boolean withDialog = false;//todo: give another API with dialog
        mSimpleFacebook.publish(feed, withDialog, new OnPublishListener() {
            @Override
            public void onComplete(String postId) {
                super.onComplete(postId);
                socialActionListener.success();
            }

            @Override
            public void onException(Throwable throwable) {
                super.onException(throwable);
                socialActionListener.fail("onException: " + throwable.getLocalizedMessage());
            }

            @Override
            public void onFail(String reason) {
                super.onFail(reason);
                socialActionListener.fail("onFail: " + reason);
            }
        });
    }

    @Override
    public void updateStory(String message, String name, String caption, String description, String link, String picture,
                            final SocialCallbacks.SocialActionListener socialActionListener) {
        Feed feed = new Feed.Builder()
                .setMessage(message)
                .setName(name)
                .setCaption(caption)
                .setDescription(description)
                .setLink(link)
                .setPicture(picture)
                .build();

        boolean withDialog = false;//todo: give another API with dialog
        mSimpleFacebook.publish(feed, withDialog, new OnPublishListener() {
            @Override
            public void onComplete(String postId) {
                super.onComplete(postId);
                socialActionListener.success();
            }

            @Override
            public void onException(Throwable throwable) {
                super.onException(throwable);
                socialActionListener.fail("onException: " + throwable.getLocalizedMessage());
            }

            @Override
            public void onFail(String reason) {
                super.onFail(reason);
                socialActionListener.fail("onFail: " + reason);
            }
        });
    }

    private void fbUpdateStory() {
        // set object to be shared
        Story.StoryObject storyObject = new Story.StoryObject.Builder()
                .setUrl("http://romkuapps.com/github/simple-facebook/object-apple.html")
                .setNoun("food")
                .build();

        // set action to be done
        Story.StoryAction storyAction = new Story.StoryAction.Builder()
                .setAction("eat")
                .addProperty("taste", "sweet")
                .build();

        // build story
        Story story = new Story.Builder()
                .setObject(storyObject)
                .setAction(storyAction)
                .build();

        mSimpleFacebook.publish(story, new OnPublishListener() {
            @Override
            public void onComplete(String response) {
                super.onComplete(response);
            }

            @Override
            public void onException(Throwable throwable) {
                super.onException(throwable);
            }

            @Override
            public void onFail(String reason) {
                super.onFail(reason);
            }

            @Override
            public void onThinking() {
                super.onThinking();
            }
        });
    }

    @Override
    public void getContacts(final SocialCallbacks.ContactsListener contactsListener) {
        if (mSimpleFacebook != null) {
            mSimpleFacebook.getFriends(new OnFriendsListener() {
                @Override
                public void onComplete(List<Profile> response) {
                    super.onComplete(response);

                    List<UserProfile> userProfiles = new ArrayList<UserProfile>();
                    for (Profile profile : response) {
                        userProfiles.add(new UserProfile(
                                getProvider(), profile.getId(), profile.getUsername(), profile.getEmail(),
                                profile.getFirstName(), profile.getLastName()));
                    }
                    contactsListener.success(userProfiles);
                }

                @Override
                public void onException(Throwable throwable) {
                    super.onException(throwable);
                    contactsListener.fail("onException: " + throwable.getLocalizedMessage());
                }

                @Override
                public void onFail(String reason) {
                    contactsListener.fail("onFail: " + reason);
                }
            });
        }
    }

    @Override
    public void getFeeds(final SocialCallbacks.FeedsListener feedsListener) {
        mSimpleFacebook.getPosts(Post.PostType.ALL, new OnPostsListener() {
            @Override
            public void onComplete(List<Post> posts) {
                super.onComplete(posts);
                List<String> feeds = new ArrayList<String>();
                for (Post post : posts) {
                    feeds.add(post.getMessage());
                }
                feedsListener.success(feeds);
            }

            @Override
            public void onException(Throwable throwable) {
                super.onException(throwable);
                feedsListener.fail("onException: " + throwable.getLocalizedMessage());
            }

            @Override
            public void onFail(String reason) {
                super.onFail(reason);
                feedsListener.fail("onFail: " + reason);
            }
        });
    }

    @Override
    public void uploadImage(String message, String filePath, final SocialCallbacks.SocialActionListener socialActionListener) {
        Bitmap bitmap = BitmapFactory.decodeFile(filePath);
        Photo photo = new Photo.Builder()
                .setImage(bitmap)
                .setName(message)
//                .setPlace("110619208966868")
                .build();

        mSimpleFacebook.publish(photo, new OnPublishListener() {
            @Override
            public void onComplete(String response) {
                super.onComplete(response);
                socialActionListener.success();
            }

            @Override
            public void onException(Throwable throwable) {
                super.onException(throwable);
                socialActionListener.fail("onException:" + throwable.getLocalizedMessage());
            }

            @Override
            public void onFail(String reason) {
                super.onFail(reason);
                socialActionListener.fail("fail:" + reason);
            }

            @Override
            public void onThinking() {
                super.onThinking();
            }
        });
    }

    @Override
    public void uploadImage(String message, String fileName, Bitmap bitmap, int jpegQuality, final SocialCallbacks.SocialActionListener socialActionListener) {
        Photo photo = new Photo.Builder()
                .setImage(bitmap)
                .setName(message)
//                .setPlace("110619208966868")
                .build();

        mSimpleFacebook.publish(photo, new OnPublishListener() {
            @Override
            public void onComplete(String response) {
                super.onComplete(response);
                socialActionListener.success();
            }

            @Override
            public void onException(Throwable throwable) {
                super.onException(throwable);
                socialActionListener.fail("onException:" + throwable.getLocalizedMessage());
            }

            @Override
            public void onFail(String reason) {
                super.onFail(reason);
                socialActionListener.fail("fail:" + reason);
            }

            @Override
            public void onThinking() {
                super.onThinking();
            }
        });
    }

    @Override
    public Provider getProvider() {
        return Provider.FACEBOOK;
    }

    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        if (mSimpleFacebook != null) {
            mSimpleFacebook.onActivityResult(activity, requestCode, resultCode, data);
        }
    }

    @Override
    public void onResume(Activity activity) {
        mSimpleFacebook = SimpleFacebook.getInstance(activity);
    }
}

