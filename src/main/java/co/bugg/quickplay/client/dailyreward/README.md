The Daily Rewards webpage doesn't provide a friendly API. Therefore, Quickplay has to scrape the page to get relevant data, which is messy.

As of 1.0.1, the only files that contain relevant information are the original document requested and the subsequent `app.js` request.
The main document will contain application data, the security key, and translations. `app.js` will contain game translations and
some other misc. data that might be useful in the future.

Scraping and formatting is mostly straightforward, with two exceptions:
* Housing blocks
* Vanity items

### Housing Items
Housing block data is contained in `package`, which is then refactored to `packageInfo` in [DailyRewardHandler.java](DailyRewardHandler.java).
This seems to be a general field and `packageInfo` could be used for other things in the future. Regardless, Housing block
translations are weird as well. `packageInfo` will start with `specialoccasion_reward_card_skull_`, which needs to be refactored
into `housing.skull.` before translation, as that's what the i18n translations start with. This is static. I am sure there
is a better way to do it (i.e. pulling from `app.js`) but that isn't worth it at the moment.

### Vanity Items
Vanity items are even worse, although not as static by the looks of it. At the moment, there are three "categories" for vanity
items, those being:
* Suits (`suit`)
* Emotes (`emote`)
* Gestures (`gesture`)

A vanity item will be signified with the `add_vanity` untranslated reward name. This key does not have a matching translation.
Instead, translations need to be manually calculated. In general, vanities seem to take up the general form `category_name_subtype`.
As of now, subtype is only applicable to suits by the looks of it. I believe names cannot contain underscores, or at least
none of them do as of now. Vanity i18n descriptions take up the format `vanity.{$category}.description`. Vanity i18n names take
up the format `vanity.{$category}_{$name}`. Vanity  i18n subtypes for armor take up the format `vanity.armor.{$subtype}`.
No other subtypes are available at the moment.