#!/usr/bin/python

import os
import sys
import time
import subprocess

NIGHTLY_BIN_PATH = "/Users/perdisci/Desktop/firefox_sources/mozilla-release-instrumented-compiled/obj-x86_64-apple-darwin10.8.0/dist/Nightly.app/Contents/MacOS/firefox"
NIGHTLY_PROFILE_NAME = "CrawlerCacheTest"
NIGHTLY_USED_PROFILE_PATH = "/Users/perdisci/Library/Application\ Support/Firefox/Profiles/46d3dsu0.CrawlerCacheTest"
NIGHTLY_CLEAN_PROFILE_PATH = "/Users/perdisci/Library/Application\ Support/Firefox/Profiles/46d3dsu0.CrawlerCacheTest-backup"
NIGHTLY_USED_CACHE_PATH = "/Users/perdisci/Library/Caches/Firefox/Profiles/46d3dsu0.CrawlerCacheTest"
NIGHTLY_CLEAN_CACHE_PATH = "/Users/perdisci/Library/Caches/Firefox/Profiles/46d3dsu0.CrawlerCacheTest-backup"

FIREFOX_QUIT_URL = "file:///Users/perdisci/Desktop/UGA/Projects/ClickFraud/ClickMiner/clickfinder-browser/trunk/SeIDE-plugin/FirefoxCacheTest/quitForMac.html"
TOP_SITES_LIST = "top1k_Alexa_www_20121106.txt"
START_SITES_BATCH = 10
WAIT_TIME = 20
RESTART_DELAY = 5

STDOUT_FILE_1 = 'out1.tmp'
STDOUT_FILE_2 = 'out2.tmp'

def init_profile():

    command = 'rm -rf '+NIGHTLY_USED_PROFILE_PATH
    print command
    p = subprocess.Popen(command, shell=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
    p.wait()

    command = 'cp -r '+NIGHTLY_CLEAN_PROFILE_PATH+' '+NIGHTLY_USED_PROFILE_PATH
    print command
    p = subprocess.Popen(command, shell=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
    p.wait()

    command = 'rm -rf '+NIGHTLY_USED_CACHE_PATH
    print command
    p = subprocess.Popen(command, shell=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
    p.wait()

    command = 'cp -r '+NIGHTLY_CLEAN_CACHE_PATH+' '+NIGHTLY_USED_CACHE_PATH
    print command
    p = subprocess.Popen(command, shell=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
    p.wait()

    command = 'rm -f '+STDOUT_FILE_1
    print command
    p = subprocess.Popen(command, shell=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
    p.wait()

    command = 'rm -f '+STDOUT_FILE_2
    print command
    p = subprocess.Popen(command, shell=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
    p.wait()



def run_cache_test(clargs):

    # command = [NIGHTLY_BIN_PATH, '-P', NIGHTLY_PROFILE_NAME] + clargs

    clargs_str = FIREFOX_QUIT_URL + ' '
    for a in clargs:
        clargs_str += ' ' + a

    command = NIGHTLY_BIN_PATH + ' -P ' + NIGHTLY_PROFILE_NAME + ' ' + clargs_str
    print command


    # p = subprocess.Popen(command, shell=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
    out = open(STDOUT_FILE_1,'w')
    p = subprocess.Popen(command, shell=True, stdout=out, stderr=subprocess.STDOUT)
    print "Firefox (1) PID=", p.pid
    # time.sleep(WAIT_TIME)
    # for line in p.stdout.readlines():
    #     print line.strip()
    # retval = p.terminate()
    retval = p.wait()
    out.close()

    # restart and reload sames websites to see if their main pages were cached
    time.sleep(RESTART_DELAY)
    out = open(STDOUT_FILE_2,'w')
    p = subprocess.Popen(command, shell=True, stdout=out, stderr=subprocess.STDOUT)
    print "Firefox (2) PID=", p.pid
    retval = p.wait()
    out.close()

    # check results
    # less out2.tmp | grep ReadFromCache | egrep "http://.+\.[a-z]{1,4}/$"

    command = 'less out2.tmp | grep ReadFromCache | egrep "URL=http://www\.[a-zA-Z0-9\_\.\-]+\.[a-z]{1,4}/$" >> cache_test_results.log'
    print command
    p = subprocess.Popen(command, shell=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
    for line in p.stdout.readlines():
        print line.strip()
    retval = p.wait()



def main():

    # read all top sites
    f = open(TOP_SITES_LIST,'r')
    sites = f.readlines()
    f.close()

    # read top sites in batches of START_SITES_BATCH
    i = 1
    clargs = []
    init_profile()

    for s in sites:
        if not (i % START_SITES_BATCH) == 0: 
            clargs.append(s.strip())
            i += 1
        else: # includes the last element
            clargs.append(s.strip())
            cached_sites = run_cache_test(clargs)
            # reset
            init_profile()
            clargs = []
            i += 1


if __name__ == "__main__":
    main()
