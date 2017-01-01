---
title: BeanTemp
description: Temperature logger for LightBlue Beans
author: Fredrik Erlandsson
tags: android, bluetooth, temperature, logger, arduino
created:  2016 Dec 15

---

BeanTemp
========

An Android application that accesses [LightBlue Beans] using the [Bean-Android SDK] at a given interval (currently statically configured to 10 minutes). The app scans for nearby Beans and connects to all of them and read each Bean's temperature and battery percentage. The data from the connected Beans are submitted to a webhook as a json GET package.

<img src="/doc/Screenshot_20161216-123925.png?raw=true" alt="Lock Screen" width="40%" />
<img src="/doc/Screenshot_20161216-123939.png?raw=true" alt="Main Screen" width="40%" />


[Fredrik Erlandsson]: https://github.com/fredrike
[LightBlue Beans]: https://punchthrough.com/bean
[Bean-Android SDK]: https://github.com/PunchThrough/bean-sdk-android
