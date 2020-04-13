# <img src="src/main/webapp/images/24x24/shortcutjob.png"> Shortcut Job Plugin

[![Build Status](https://travis-ci.org/mlasevich/shortcut-job-plugin.svg?branch=master)](https://travis-ci.org/mlasevich/shortcut-job-plugin)


## Introduction

Sometimes you want to have everything in one place, even if in reality it is spread across
a number of folders or even a number of jenkins servers. This plugin helps with that by allowing
you to create an item that is a shortcut to any URL - either absolute, or relative to the 
web server or relative to Jenkins root.

## Usage

Simply create a new item with type Shortcut - enter a name for it and URL, and you are done.

***Job Options:***

* **Name** - Job name
* **TargetUrl** - Target URL in one of three formats:
    1. Full URL stating with http:// or https://
        * ***Example:***

         If your jenkins server is at *https://my.jenkins.io/jenkins* and you moved team "team"'s
         folder to a new jenkins server with url of *https://team.jenkins.io/jenkins* You can
         create an item in old server with **TargetUrl** of `https://team.jenkins.io/jenkins`
    2. Relative URL starting with / - this will be relative to the root of current web server.
        * ***Example:***

          If your jenkins server is at *https://my.jenkins.io/jenkins* - a value of
            `/anotherapp/path` will redirect you to *https://my.jenkins.io/anotherapp/path*
    3. Relative URL not stating with / - this will be relative to your Jenkins install
        * ***Example:***

          If your jenkins server is at *https://my.jenkins.io/jenkins* - a value of
            `configure` will redirect you to *https://my.jenkins.io/jenkins/configure*

* **enabled** - This flag allows you to troubleshoot your shortcut. If not enabled, clicking
  on the item will display target instead of redirecting you.

## Known issues

* Currently when you save your item, you will be redirected to the URL immediately (if
  item is enabled)
* Renaming of items broke sometime before initial release
* Absolute lack of unit tests

## Changelog

* 0.1 - Initial Version

  Initial version
