#!/usr/bin/env python
"""
 Copyright 2010 EDL FOUNDATION

 Licensed under the EUPL, Version 1.1 or as soon they
 will be approved by the European Commission - subsequent
 versions of the EUPL (the "Licence");
 you may not use this work except in compliance with the
 Licence.
 You may obtain a copy of the Licence at:

 http://ec.europa.eu/idabc/eupl

 Unless required by applicable law or agreed to in
 writing, software distrlogibuted under the Licence is
 distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 express or implied.
 See the Licence for the specific language governing
 permissions and limitations under the Licence.


 Created by: Jacob Lundqvist (Jacob.Lundqvist@gmail.com)


 ThumbChecker for items Europeana should cache


 Please note, that a some bad urls, might be resulting in a 10 min timeout,
 if thats the case, that provider is aborted after the timeout, if you dont get
 regular progress reports, this is propably the case, just wait 15 mins if you
 still havent seen any indication that this is the case, you can propably asume
 that this util has crashed.


 1. mini crm hantera saker
 2. aquasition file / repox
 3  validation import

 00725_A_DE_Landesarchiv_e  eta

 mockflow


 Version see: _version below

 History:
 100223 jaclu  Initial release
 100226 jaclu  Second rev - v0.1.0, improved reporting
"""
_version = '0.1.0'

"""
collections that was disconnected
02501_L_NatLib_DIGAR_oai_ 4714/18201 (25.90%) Bad items: 1 (0.02%)
02501_L_NatLib_DIGAR_oai_dc     items: 18201    BAD ITEMS: 1 (0.0%)
 None 404 error - 503

03503_L_FR_NatLib_gallica 17/40063 (0.04%)
03503_L_FR_NatLib_gallica_images_dc     items: 40063

03912_Ag_FR_MCC_memoireSA 230064/409707 (56.15%) Bad items: 860 (0.37%)
03912_Ag_FR_MCC_memoireSAP      items: 409707   BAD ITEMS: 860 (0.2%)

09403_Ag_AT_ELocal 208/13550 (1.54%) Bad items: 5 (2.40%)
09403_Ag_AT_ELocal      items: 13550    BAD ITEMS: 5 (0.0%)
 unknown bad content type: video/mpeg
 unknown bad content type: image/tiff (repeated 85 times)

09414_Ag_NO_ELocal 62500/62802 (99.52%) Bad items: 2 (0.00%)
09414_Ag_NO_ELocal      items: 62802    BAD ITEMS: 2 (0.0%)
 unknown bad content type: video/x-ms-wmv (repeated 2 times)
 None 404 error - 400 (repeated 2 times)
 unknown bad content type: audio/x-pn-realaudio

 09428_Ag_DE_ELocal 1955/2203 (88.74%)    eta: 14:54:43
 09428_Ag_DE_ELocal      items: 2203

"""

import os
import sys
import time
import socket
import urllib2
import urlparse
import threading



img = 'http://europeana.eu/portal/images/think_culture_logo_top_5.jpg'
ss = 'http://alma.ablm.se/w2ko2k.tgsz'

ITM_START = 'itm_start'
ITM_COUNT = 'itm_count'
ITM_DONE = 'itm_done'
ITM_OK = 'itm_ok'
ITM_BAD = 'itm_bad'
TIMEOUTS = 'timeouts'
ITM_BAD_TYPES = 'itm_bad_types'
PROVIDER_NAME = 'prov_name'
ABORT_REASON = 'abort_reason'

URL_TIMEOUT = 10

INTERVALL_PROGRES = 30
INTERVALL_REPORT = 60

MAX_NO_THREADS = 50

ABORT_LIMIT_TIMEOUTS = 50
ABORT_LIMIT_URLERROR = 50


REPORT_FILE = '/tmp/thumb_checker_report.txt'
REPORT_BAD_ITEMS = '/tmp/thumb_checker_bad_items.log'

WARNING_SAME_URL_PREFIX = 'same url used for'

all_providers = {}

lock_collections = threading.Lock()


class Collection(object):
    def __init__(self, fname, host_name, debug_lvl=1):
        self.fname = fname # what file to read
        self.host_name = host_name # used when threading to keep track of host
        self.debug_lvl = debug_lvl
        self.qname = os.path.split(fname)[1].split('_urls')[0] # do we need to keep qname??
        self.provider = self.qname[:3]
        self.collection = self.qname[:5]
        self.items_verified = 0 # items verified to exist and of reasonable mime type
        self.bad_items = {} # key is complaint, value is list of urls
        self.aboort_reason = '' # if set this is why collection was aborted (human readable)
        self.timeout_counter = 0  # number of timeouts
        self.url_error = 0
        self.is_completed = False
        self._thread = None
        self.mime_ok = ['audio/mpeg',
                        'image/gif',
                        'image/jpg',
                        'image/jpeg',
                        'image/png',
                        'image/tiff',
                        'video/mpeg',
                        'video/x-ms-wmv',
                        'application/pdf',
                        ]
        self.mime_not_good = ['text/html',] # mime types tested to be bad
        self.time_started = time.time()
        self.log('+    %s Creting Collection obj' % self.collection, 8)
        self.items = self.generate_urllist() # still unprocessed items
        self.item_count = len(self.items) # number of items
        self.bind_to_provider() # for report grouping etc
        if self.time_started + 3 < time.time():
            self.log('++   %s Collection obj Created' % self.collection, 1)
        return



    def check_item(self):
        "Do the next item, if result is false, this collection is completed."
        if self.items:
            url = self.items.pop()
        else:
            url = ''
        if not url:
            return self.file_is_completed()
        try:
            itm = urllib2.urlopen(url)#,timeout=URL_TIMEOUT)
        except urllib2.HTTPError, e:
            self.add_bad_item('http error %i' % e.code, url)
            if e.code == 403: # forbidden
                self.aboort_reason = '403 response'
                self.items = []
                self.file_is_completed()
                return self.file_is_completed()
            return True
        except urllib2.URLError, e:
            b = True
            if str(e.reason) == 'timed out':
                reason = 'timed out'
                self.timeout_counter += 1
                if self.timeout_counter >= ABORT_LIMIT_TIMEOUTS:
                    b = self.file_is_completed('too many timeouts')
            else:
                reason = 'URLError %s' % str(e.reason)
                self.url_error += 1
                if self.url_error >= ABORT_LIMIT_URLERROR:
                    b = self.file_is_completed('too many urlerrors')
            self.add_bad_item(reason, url)
            return b

        if itm.code != 200:
            self.add_bad_item('HTML status: %i' % itm.code, url)
            return True
        content_t = itm.headers['content-type']#.split(';')[0]
        if content_t in (self.mime_ok):
            self.items_verified += 1
        else:
            if content_t in self.mime_not_good:
                self.add_bad_item(content_t, url)
            else:
                if self.odd_mime_is_valid_file(itm, content_t):
                    self.mime_ok.append(content_t)
                    self.items_verified += 1
                else:
                    self.mime_not_good.append(content_t)
                    self.add_bad_item(content_t, url)
        return True


    def set_thread(self, thr):
        "save a ref to the thread that is running this if threaded."
        self._thread = thr


    def get_thread(self):
        return self._thread

    def items_done(self):
        return self.item_count - self.items_remaining()


    def items_remaining(self):
        return len(self.items)


    def items_verified(self):
        return self.items_done()


    def bad_items_count(self):
        r = 0
        lst = self.bad_items_summary()
        for reason, count in lst:
            r += count
        return r


    def bad_items_summary(self):
        lst = []
        for reason in self.bad_items.keys():
            lst.append( (reason, len(self.bad_items[reason])) )
        lst.sort()
        return lst

    #  -----------   internals   ---------------
    def add_bad_item(self, complaint, url):
        if not self.bad_items.has_key(complaint):
            self.bad_items[complaint] = []
        self.bad_items[complaint].append(url)
        #print '%s - %s' % (complaint, url)

    def bind_to_provider(self):
        if not self.provider in all_providers.keys():
            all_providers[self.provider] = {}
        all_providers[self.provider][self.collection] = self


    def file_is_completed(self, reason=''):
        "Doing final cleanup when processing is done."
        if reason:
            self.aboort_reason = reason
        self.is_completed = True
        return False

    def odd_mime_is_valid_file(self, itm, content_t):
        #url = '/Users/jaclu/Documents/ablm/reserakning.pdf'
        lock_collections.acquire()
        self.log('???  %s Checking strange mimetype: %s' % (self.collection, content_t), 7)
        if content_t.split(';')[0] in self.mime_ok:
            # only check when base mime is valid, but broken webserver gives
            # multiple fields as content type
            excode = True
            #img_data = itm.read()
            #im = Image.fromstring('rw',len(img_data),img_data)
            ##fp = open('/dev/null','w')
            ##excode = subprocess.call(['identify', url],
            ##                         stdout=fp,
            ##                         stderr=fp,)
        else:
            excode = 1
        if excode:
            self.log('!!!  %s invalid_filetype: %s' % (self.collection, itm.url), 6)
            b = False
        else:
            self.log('     %s seems to be a acceptable filetype: %s' % (self.collection, itm.url), 6)
            b = True
        lock_collections.release()
        return b


    def log(self,msg,lvl=2):
        if self.debug_lvl < lvl:
            return
        print msg
    #
    #  Parse file, extract all urls
    #
    def generate_urllist(self):
        fp = open(self.fname)
        lines = fp.readlines()
        fp.close()
        urllist = []
        dup_urls = {}
        t0 = time.time()
        counted = old_counted = 0
        for line_lf in lines:# lines:#[:12]:
            line = line_lf[:-1]
            if not line or line[0]=='#':
                continue
            url = line.split('wget ')[1].split('-O')[0].strip()
            if url not in urllist:
                urllist.append(url)
            else:
                dup_urls[url] = dup_urls.get(url,0) + 1
            counted +=1
            if t0 + INTERVALL_PROGRES < time.time():
                self.log('reading file %s - %i / %i done (rate %i)' % (self.fname, counted, len(lines), counted - old_counted), 2)
                old_counted = counted
                t0 = time.time()
        for key in dup_urls.keys():
            self.add_bad_item('%s %s items' % (WARNING_SAME_URL_PREFIX, dup_urls[key]), key)
        return urllist







class VerifyProvider(object):
    def __init__(self, debug_lvl=4):
        self.debug_lvl = debug_lvl
        self.extra_msgs = {}
        self.in_progress = {}
        self.reports = {}
        self.is_running = False
        self.q_waiting = []
        self.running_threads = {}

    def run(self, param):
        if self.is_running:
            print 'Aborting, this instance is already running'
            return 1
        self.is_running = True
        if os.path.isdir(param):
            self.do_dir(param)
        else:
            col = Collection(param, '', self.debug_lvl)
            self.do_file(col, show_progress=True)
        self.create_report(to_file=False)
        self.create_report(to_file=True)
        self.is_running = False

    #=============  internals  ==============

    #
    #   Loop on all files in a dir
    #
    def do_dir(self, ddir):
        files = os.listdir(ddir)
        self.q_waiting = []
        for fname in files:
            if os.path.isdir(fname):
                continue # skip subdirs
            host_name = self.get_server_hostname(os.path.join(ddir, fname))
            self.q_waiting.append((host_name, os.path.join(ddir,fname)))
        t0 = t1 = time.time()
        self.start_free_hosts(True)
        while threading.activeCount() > 1 or self.q_waiting:
            if t0 + INTERVALL_PROGRES < time.time():
                self.threads_cleanup()
                self.show_progress()
                t0 = time.time()
            if t1 + INTERVALL_REPORT < time.time():
                self.create_report(to_file=True)
                t1 = time.time()
            time.sleep(1)
            self.start_free_hosts()

    #
    #  Check all the items from one file (collection)
    #
    def do_file(self, collection, show_progress=False):
        self.log('>>>  %s processing starting' % collection.collection, 5)
        t0 = t1 = time.time()
        while True:
            if show_progress:
                if t0 + INTERVALL_PROGRES < time.time():
                    self.show_progress()
                    t0 = time.time()
                if t1 + INTERVALL_REPORT < time.time():
                    self.create_report(to_file=True)
                    t1 = time.time()
            if not collection.check_item():
                break
            pass
        self.log('<<<  %s processing completed' % collection.collection, 5)
        return


    def get_server_hostname(self, fname):
        hostname = ''
        fp = open(fname)
        lines = fp.readlines()
        fp.close()
        for line in lines:
            if 'wget' in line:
                url = line.split('wget ')[1].split('-O')[0].strip()
                hostname = urlparse.urlsplit(url).hostname
                break
        return hostname


    #
    #  Progress display
    #
    def show_progress(self):
        remaining = 0
        eta_max = 0
        providers = all_providers.keys()
        providers.sort()
        print
        for provider in providers:
            collecions = all_providers[provider].keys()
            collecions.sort()
            for collection in collecions:
                col = all_providers[provider][collection]
                if col.is_completed:
                    continue
                perc_done = 100 * float(col.items_done()) / col.item_count
                remaining += len(col.items)
                eta = self.eta_calculate(time.time() - col.time_started, perc_done)
                eta_num = self.numeric_eta(eta)
                if eta_num > eta_max:
                    eta_max = eta_num
                    s_eta_max = eta

                msg = '%25.25s %i/%i (%.2f%%)  \teta: %s' % (col.qname,
                                                             col.items_done(),
                                                             col.item_count,
                                                             perc_done, eta)

                if col.bad_items_count():
                    p = 100 * col.bad_items_count() / float(col.items_done())
                    msg += ' - Bad items: %i (%.2f%%)' % (col.bad_items_count(), p)
                self.log(msg, 2)
        if threading.activeCount() > 1:
            print '==== threadcount %i \twaiting collections %i \tcurrently pending files to check %i eta: %s' % (threading.activeCount() - 1,
                                                                                                                  len(self.q_waiting),
                                                                                                                  remaining,
                                                                                                                  s_eta_max)
        self.time_progess = time.time()


    def eta_calculate(self, elapsed_time, perc_done):
        if perc_done == 0:
            perc_done = 0.001
        eta = elapsed_time / float(perc_done/100)
        h = 0
        while eta > 3600:
            h += 1
            eta -= 3600
        m = 0
        while eta > 60:
            m += 1
            eta -= 60
        if h:
            s = '%i:' % h
        else:
            s = '0:'
        if m:
            s += '%02i' % m
        else:
            s += '00'
        s += ':%02i' % eta
        return s

    def numeric_eta(self, eta):
        h,m,s = eta.split(':')
        ih = int(h)
        im = int(m)
        iis = int(s)
        return (ih,im,iis)


    #
    #   Report generation
    #
    def add_report(self, qname, q):
        provider = qname[:3]
        collection = qname[:5]
        if not self.reports.has_key(provider):
            self.reports[provider] = {}
        self.reports[provider][collection] = q


    def create_report(self, to_file=False):
        if to_file:
            self.report_fp = open(REPORT_FILE, 'w')
            if REPORT_BAD_ITEMS:
                if os.path.exists(REPORT_BAD_ITEMS):
                    os.remove(REPORT_BAD_ITEMS)
            else:
                print '*** Bad items report filename is empty!'
                sys.exit(1)
        else:
            self.report_fp = sys.stdout
        self.report_print('\n')
        self.report_print('=' * 80)
        self.report_print('\n\tAvailability of thumbnail items on servers\n')
        self.report_print('')
        bfound_bad_items = False
        providers = all_providers.keys()
        providers.sort()
        for provider in providers:
            collecions = all_providers[provider].keys()
            collecions.sort()
            prov_item_count = 0    # summary for provider
            prov_item_verified = 0 # summary for provider
            prov_item_bad = 0      # summary for provider
            for collection in collecions:
                col = all_providers[provider][collection]
                if col.items_verified < col.item_count:
                    s = 'items: %i - verified: %i' % (col.item_count, col.items_verified)
                else:
                    s = 'items: %i' % col.items_verified
                msg = '%s \t%s' % (col.qname, s)
                if col.timeout_counter:
                    msg += '\ttimeouts: %i\t<===' % col.timeout_counter
                bad_count = col.bad_items_count()
                if bad_count:
                    msg += '\tBAD ITEMS: %i (%.1f%%)' % (bad_count,
                                                          100 * bad_count/float(col.item_count))
                if col.aboort_reason:
                    msg += '  %s  <===== Aborted' % col.aboort_reason
                self.report_print(msg)
                prov_item_count += col.item_count
                prov_item_verified += col.items_verified
                prov_item_bad += bad_count

                problems = col.bad_items_summary()
                if problems:
                    bfound_bad_items = True
                    if to_file:
                        fp = open(REPORT_BAD_ITEMS, 'a+')
                        fp.write('============   Problems in %s   ============\n' % collection)
                    for reason, count in problems:
                        if WARNING_SAME_URL_PREFIX in reason:
                            msg = '  ' + reason
                        else:
                            msg = '  %s - %i times' % (reason, count)
                        self.report_print(msg)
                        if to_file:
                            fp.write('-------   %s   -------\n' % reason)
                            for url in col.bad_items[reason]:
                                fp.write('%s\n' % url)
                    if to_file:
                        fp.close()

            msg = '%s \titems:%i verified: %i' % (provider, prov_item_count, prov_item_verified)
            if prov_item_bad:
                msg += ' \tBAD ITEMS: %i (%.1f%%)' % (prov_item_bad,
                                                      100 * prov_item_bad/float(prov_item_count))
            if not prov_item_verified:
                msg += ' \t<-- no items!'
            self.report_print(msg)
            self.report_print('\n')
            self.report_print('-' * 80)
            self.report_print('\n')
        if to_file:
            self.report_fp.close()
        return

    def report_print(self, msg):
        self.report_fp.write('%s\n' % msg)
        self.report_fp.flush()


    def parse_report_name(self,fname):
        s = os.path.split(fname)[1]
        return s

    #
    #  Threading
    #
    def start_free_hosts(self, blimited=False):
        i = 0
        for host_name, fname in self.q_waiting:
            i += 1
            if blimited and i > 10:
                break
            if len(self.running_threads) > MAX_NO_THREADS:
                break
            if not self.is_host_name_used(host_name):
                self.q_waiting.remove((host_name, fname))
                col = Collection(fname, host_name, self.debug_lvl)
                if not host_name:
                    self.log('---  %s Skipping empty collection' % col.collection, 9)
                    col.is_completed = True
                    continue
                #Add this collection to a separate thread and start it
                if self.is_host_name_used(host_name):
                    print '*** Serious error, attempt to run towards occupied host'
                    print host_name, fname
                    sys.exit(1)

                time.sleep(0.1)
                t = threading.Thread(target=self.run_thread,name=col.collection,args=(col,))
                self.log('++   %s Thread created' % col.collection, 9)
                self.running_threads[host_name] = {'thread': t,
                                                   'collection': col, # for easy access when detecting
                                                                      # abandoned threads
                                                   }
                col.set_thread(t)
                t.start()
        return

    def is_host_name_used(self, host_name):
        return host_name in self.running_threads.keys()


    def run_thread(self, collection):
        self.log('+++  %s starting thread' % collection.collection, 9)
        self.do_file(collection)

    def threads_cleanup(self):
        i1 = self.no_not_completed()
        for host_name in self.running_threads.keys()[:]:
            col = self.running_threads[host_name]['collection']
            thr = self.running_threads[host_name]['thread']
            if not thr.isAlive():
                if not col.is_completed:
                    self.log('***  %s thread done, but not collection (by host_name)' % col.collection, -1)
                    pass
                del self.running_threads[host_name]

        providers = all_providers.keys()
        providers.sort()
        for provider in providers:
            collecions = all_providers[provider].keys()
            collecions.sort()
            for collection in collecions:
                col = all_providers[provider][collection]
                if col._thread and (not col._thread.isAlive()):
                    if not col.is_completed:
                        self.log('***  %s thread done, but not collection (by collection)' % col.collection, -1)
                        col.is_completed = True

        i2 = self.no_not_completed()
        if i1 != i2:
            self.log('#### thread cleaning, before: %i   after: %i' % (i1, i2),1)
        return

    def no_not_completed(self):
        i = 0
        providers = all_providers.keys()
        providers.sort()
        for provider in providers:
            collecions = all_providers[provider].keys()
            collecions.sort()
            for collection in collecions:
                col = all_providers[provider][collection]
                if not col.is_completed:
                    i += 1
        return i
    #
    #   Generic things
    #
    def log(self,msg,lvl=2):
        if self.debug_lvl < lvl:
            return
        print msg


try:
    p = sys.argv[1]
except:
    print 'Run with dir to cache output as param!'
    print 'optional second param is where to store temp reports'
    print 'othervise %s will be used' % REPORT_FILE
    sys.exit(1)

if len(sys.argv) > 2:
    REPORT_FILE = sys.argv[2]


#socket._GLOBAL_DEFAULT_TIMEOUT = 10

print 'thumb_checker - version', _version

vp = VerifyProvider(debug_lvl=9)
vp.run(p)