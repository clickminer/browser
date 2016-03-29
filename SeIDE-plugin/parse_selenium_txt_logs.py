#!/usr/bin/python

import re
import sys
import logging
import json
import urlparse

logging.basicConfig(filename='parser.log',level=logging.DEBUG)


class Window:
    """Description of a browser window"""
    
    def __init__(self):
        self.id = None       # window ID string (e.g., 'W1')
        self.h_len = None    # window history length (e.g., 1) 
        self.opener = None   # ID of the window that opened this window (may be None)

    def __str__(self):
        return "W:%s, H:%s, O:%s" % (self.id, self.h_len, self.opener)

    def hash(self):
        return str(self)
    
    def state(self):
        retval = dict()
        retval["id"] = self.id
        retval["h_len"] = self.h_len
        retval["opener"] = self.opener
        return retval
    
    def from_state(self, state):
        if state is not None:
            self.id = state.get("id")
            self.h_len = state.get("h_len")
            self.opener = state.get("opener")
    
    def to_json(self):
        return json.dumps(self.state())
        
    def from_json(self, s):
        state = json.loads(s)
        self.from_state(state) 
        

class Frame:
    """Description of a browser frame"""

    def __init__(self):
        self.id = None    # frame ID string (e.g., 'W1')
        self.w_id = None  # ID of the window to which the frame belongs
        self.path = None    # Sequence of frame-IDs
        self.path = []

    def __str__(self):
        return "F:%s, W:%s, P:%s" % (self.id, self.w_id, self.path)

    def hash(self):
        return str(self)
    
    def state(self):
        retval = dict()
        retval["id"] = self.id
        retval["w_id"] = self.w_id
        retval["path"] = self.path
        return retval
    
    def from_state(self, state):
        if state is not None:
            self.id = state.get("id")
            self.w_id = state.get("w_id")
            self.path = state.get("path")
    
    def to_json(self):
        return json.dumps(self.state())
        
    def from_json(self, s):
        state = json.loads(s)
        self.from_state(state) 

class Page:
    """Description of a browser page"""

    def __init__(self):
        self.window = None    # the window to which this page belongs
        self.frame = None     # the frame (if any) to which page belongs
        self.title = None     # page title
        self.url = None       # page URL
        self.referrer = None  # page Referrer
        self.timestamp = None # timestamp at which page was last accessed

    def getURLPath(self):
        if not self.url.startswith('http://'):
            return "[ERROR PARSING PAGE URL]"

        if self.url.endswith('/'):
            return self.url

        if len(self.url.split('/'))<=3: # e.g., if url = http://www.example.com
            return self.url+'/'

        tokens = self.url.split('?')[0].split('/') # make sure we only take the url before possible variables
        upath = ''
        for i in range(len(tokens)-1):
            upath += tokens[i]+'/'

        # print "===> PATH =", upath
        return upath

    def getURLDomain(self): # e.g., if url is 'http://example.com/test.html' it returns 'http://example.com'
        pattern = re.compile('[a-z]{1,6}://')
        if not pattern.match(self.url):
            return None
        tokens = self.url.split('/')
        if len(tokens) < 3: 
            return None

        d = tokens[0]+'//'+tokens[2]

        # print "===> DOMAIN =", d
        return d


    def __str__(self):
        return "W:%s, F:%s, T:%s, U:%s, R:%s, S:%s" % (self.window, self.frame, self.title, self.url, self.referrer, self.timestamp)

    def hash(self):
        # notice that the timestamp is not included because it can be updated 
        # due to different accesses to the same page
        #
        # notice also that reopening the same page more than once in the same window
        # will increase the window history length, and therefore change the hash
        return "W:%s, F:%s, U:%s, R:%s" % (self.window, self.frame, self.url, self.referrer)
    
    def state(self):
        retval = dict()
        if self.window is not None:
            retval["window"] = self.window.state()
        else:
            retval["window"] = None
            
        if self.frame is not None:
            retval["frame"] = self.frame.state()
        else:
            retval["frame"] = None
            
        retval["title"] = self.title
        retval["url"] = self.url
        retval["referer"] = self.referrer
        retval["timestamp"] = self.timestamp
        return retval
    
    def from_state(self, state):
        if state is not None:
            winstate = state.get("window")
            if winstate is not None:  
                window = Window()
                window.from_state(winstate)
                self.window = window
            else:
                self.window = None
            
            
            framestate = state.get("frame")
            if framestate is not None:           
                frame = Frame()
                frame.from_state(framestate)
                self.frame = frame
            else:
                self.frame = None
            
            self.title = state.get("title")
            self.url = state.get("url")
            self.referrer = state.get("referer")
            self.timestamp = state.get("timestamp")
    
    def to_json(self):
        return json.dumps(self.state())
        
    def from_json(self, s):
        state = json.loads(s)
        self.from_state(state)


class Click:

    # click handler placeholders
    JS_EMBEDDED_H = '[javascipt+embedded_object]'
    EMBEDDED_H = '[embedded_object]'
    JS_H = '[javascipt]'

    def __init__(self):
        self.origin = None          # Page in which click happened
        self.destination = None     # Page in which click destination content is loaded
        self.handlers = None        # possible handlers of the click
        self.target_url = None      # the (absolute or relative) url in the first href associated with the clicked object
        self.target_locator = None  # the path composed of locators to this object
        self.target_object = None   # the object that was clicked (e.g., [object HTMLDivElement]), or url in case object contains 'href'
        self.timestamp = None       # time of click
        self.confirmed = False
        self.loaded_from_cache = False

    @staticmethod
    def isAnchor(url):
        if url.strip().startswith('#'):
            return True
        return False

    @staticmethod
    def isAbsoluteURL(url):
        if not url: return False
        pattern = re.compile('[a-z]{1,6}://')
        if pattern.match(url):
            return True
        return False

    @staticmethod
    def isRelativeToPathURL(url):
        if not url: return False

        # include the following cases:
        # some people use JS bad practices 
        # (e.g., <a href="javascript:void(0)" onclick="myJsFunc(); return false;">Link</a>)
        if not Click.isAbsoluteURL(url) and not Click.isRelativeToDomainURL(url) and not Click.isAnchor(url)\
           and not url.strip().lower().startswith('javascript:'): 
            return True
        return False

    @staticmethod
    def isRelativeToDomainURL(url):
        if not url: return False
        if url.startswith('/'):
            return True
        return False

    def isOnEmbeddedObject(self):
        if not self.target_object:
            return False
        # [object XrayWrapper [object HTMLEmbedElement]]
        if "XrayWrapper" in self.target_object or "HTMLEmbedElement" in self.target_object: 
            return True
        return False

    def hasHandlers(self):
        return len(self.handlers)>0

    # returns a full URL, even if target_url is relative
    # returns 'unknown' if the click was on an embedded object or there was a non-href handler present
    # returns None in all other cases
    def getAbsoluteTargetURL(self): # returns a full URL, even if target_url is relative
        if self.target_url:
            tu = self.target_url
            if Click.isAbsoluteURL(tu):
                return tu
            elif Click.isRelativeToDomainURL(tu):
                d = self.origin.getURLDomain()
                return d + tu
            elif Click.isRelativeToPathURL(tu):
                return self.origin.getURLPath() + tu
        elif self.isOnEmbeddedObject() and self.hasHandlers():
            return Click.JS_EMBEDDED_H
        elif self.hasHandlers():
            return Click.JS_H
        elif self.isOnEmbeddedObject():
            return Click.EMBEDDED_H
        return None

    def __str__(self):
        return "O:%s, D:%s, U:%s, T:%s" % (self.origin, self.destination, self.getAbsoluteTargetURL(), self.timestamp)

    def state(self):
        retval = dict()
        if self.origin is not None:
            retval["origin"] = self.origin.state()
        else:
            retval["origin"] = None
            
        if self.destination is not None:
            retval["destination"] = self.destination.state()
        else:
            retval["destination"] = None
        
        retval["handlers"] = self.handlers
        retval["target_url"] = self.target_url
        retval["absolute_target_url"] = self.getAbsoluteTargetURL()
        retval["target_locator"] = self.target_locator
        retval["target_object"] = self.target_object
        retval["timestamp"] = self.timestamp
        retval["confirmed"] = self.confirmed
        retval["loaded_from_cache"] = self.loaded_from_cache
        return retval;
    
    def from_state(self, state):
        if state is not None:
            originstate = state.get("origin")
            if originstate is not None:     
                origin = Page()
                origin.from_state(originstate)
                self.origin = origin
            else:
                self.origin = None
            
            
            deststate = state.get("destination")
            if deststate is not None:
                destination = Page()
                destination.from_state(deststate)
                self.destination = destination
            else:
                self.destination = None
                 
        self.handlers = state.get("handlers")
        self.target_url = state.get("target_url")
        self.target_locator = state.get("target_locator")
        self.target_object = state.get("target_object")
        self.timestamp = state.get("timestamp")
        self.confirmed = state.get("confirmed")
        self.loaded_from_cache = state.get("loaded_from_cache");
    
    def to_json(self):
        return json.dumps(self.state())
    
    def from_json(self, s):
        state = json.loads(s)
        self.from_state(state)
        

class Event:

    def __init__(self):
        self.time = None
        self.type = None
        self.within_frame = None # if false, it's a window event
        self.id = None
        self.url = None
        self.url_path = None # list of tuples (win/frame_id, url)
        self.referrer = None
        self.title = None
        self.opener = None
        self.history_len = None 
        self.locator_path = None
        self.click_handlers = None
        self.target_url = None
        self.target_object = None

        self.parse_vars = {
            'Event Time':'time',
            'Event Type':'type',
            'Window ID':'id',
            'Window URL':'url',
            'Window Path':'url_path',
            'Window Opener':'opener',
            'Window Referrer':'referrer',
            'Window Title':'title',
            'Window History Length':'history_len',
            'Frame ID':'id',
            'Frame URL':'url',
            'Frame Path':'url_path',
            'Frame Referrer':'referrer',
            'Frame Title':'title',
            'Click Handlers':'click_handlers',
            'Element Locator Path':'locator_path',
            'Target':'target_object'
        }

        self.parse_funcs = {
            'Event Time':self.parseEventTime,
            'Window ID':self.parseID,
            'Frame ID':self.parseID,
            'Window Path':self.parseURLPath,
            'Frame Path':self.parseURLPath,
            'Window Opener':self.parseWindowOpener,
            'Window History Length':self.parseWindowHistoryLen,
            'Click Handlers':self.parseClickHandlers,
            'Element Locator Path':self.parseLocatorPath,
        }
    
        self.history_len = 0 
        self.url_path = []
        self.locator_path = []
        self.click_handlers = []



    @staticmethod
    def is_start_of_new_event(l):
        if not l or len(l)<=0:
            return False
    
        res = Event.parse_log_line(l)
        if not res:
            return False

        (t1,_) = res
        if(t1 == 'Event Time'):
            return True
        return False

    @staticmethod
    def parse_log_line(l):
        logging.debug("Parsing line " +  l)            
        tokens = l.split('::')
        if len(tokens) >= 3:
            logging.debug("Tokens: (%s,%s)" % (tokens[1].strip(),tokens[2].strip()))
            return (tokens[1].strip(),tokens[2].strip())
        return None

    def add_info(self,l):
        res = self.parse_log_line(l)
        if not res:
            return

        (t,v) = res
        logging.debug("Calling for t = " +  t)            
        if t in self.parse_funcs.keys():
            logging.debug("Calling dynamic function " +  str(self.parse_funcs[t]))            
            self.parse_funcs[t](t,v)
        elif t in self.parse_vars.keys():
            logging.debug("Calling simpleAssigment() on " +  self.parse_vars[t])            
            self.simpleAssignment(self.parse_vars[t],v)

    def simpleAssignment(self,var,value):
        if not (value == 'null' or len(value)==0):
            setattr(self,var,value) # e.g., if t = 'url' sets url=v

    def parseID(self,t,v):
        self.id = v;
        self.within_frame = False # considered a window event by default
        if t.startswith('Frame'):
            self.within_frame = True

    def parseURLPath(self,t,v):
        path_tokens = v.split(" => ")
        logging.debug("Found %s url path tokens" % len(path_tokens))
        logging.debug("1-URL Path: %s %s " % (self.id,self.url_path))
        for p in path_tokens:
            url_tokens = p.split(' ')
            if len(url_tokens) >= 2:
                idval = url_tokens[0].lstrip('[').rstrip(']')
                url = url_tokens[1].strip()
                self.url_path.append((idval,url))
                logging.debug("Added URL Path: (%s,%s)" % (idval,url))
            logging.debug("2-URL Path: %s %s " % (self.id,self.url_path))
        logging.debug("3-URL Path: %s %s " % (self.id,self.url_path))

    def parseClickHandlers(self,t,v):
        handler_tokens = v.split('=> ')
        logging.debug("Found %s locator path tokens" % len(handler_tokens))
        for p in handler_tokens:
            if len(p)>0:
                p = p.strip()
                self.click_handlers.append(p)
                logging.debug("Added Click Handler: %s" % p)

                # set the event target_url to the first href url in the click handlers
                if p.startswith('href=') and len(p)>len('href=') and not self.target_url:
                    self.target_url = p[len('href='):]

    def parseLocatorPath(self,t,v):
        path_tokens = v.split(' => ')
        logging.debug("Found %s locator path tokens" % len(path_tokens))
        for p in path_tokens:
            if len(p)>0:
                p = p.strip()
                self.locator_path.append(p)
                logging.debug("Added Locator Path: %s" % p)

    def parseWindowOpener(self,t,v):
        self.opener = None
        if not v == 'null' and len(v)>0:
            self.opener = v
            logging.debug("Set Opener: %s" % self.opener)

    def parseWindowHistoryLen(self,t,v):
        if not v == 'null' and len(v)>0:
            try:
                self.history_len = int(v)
                logging.debug("Set History Len: %s" % self.history_len)
            except ValueError:
                pass

    def parseEventTime(self,t,v):
        if not v == 'null' and len(v)>0:
            try:
                self.time = int(v)
                logging.debug("Set Time: %s" % self.time)
            except ValueError:
                pass




def load_events(events_file):
    logging.debug("Loading events!")            

    events = []
    e = None
    f = open(events_file,'r')
    for l in f.readlines():
        l = l.strip()
        logging.debug("Reading Line: " + l)            

        if(Event.is_start_of_new_event(l)):
            if e:
                events.append(e) # store previous event
            logging.debug("New event: " + l)            
            e = Event()

        if e:
            e.add_info(l)

    return events



def load_http_headers(headers_file):

    new_header = True
    req_next = False
    resp_next = False

    headers = []
    url = None
    req_type = None
    resp_code = None
    location_redir = None

    f = open(headers_file,'r')
    for l in f.readlines():
        l = l.strip()
        # logging.debug("Reading Headers Line: " + l)

        if new_header and not url and re.match('^[a-z]{2,6}://.+',l):
            url = l
        elif req_next and not req_type and re.match('^(GET|POST|HEAD|OPTIONS)',l):
            req_type = l.split()[0]
        elif resp_next and not resp_code and re.match('^HTTP/[0-9]\.[0-9] [0-9]{3}',l):
            resp_code = l.split()[1]

        if new_header and len(l) == 0:
            new_header = False
            req_next = True
        elif req_next and len(l) == 0:
            req_next = False
            resp_next = True
        elif resp_next and not location_redir and l.startswith('Location:'):
            location_redir = l.split()[1]
        elif resp_next and l.startswith('--------'):
            headers.append({'url':url,'req_type':req_type,'resp_code':resp_code,'loc_redir':location_redir})
            # print "%s %s %s %s" % (url, req_type, resp_code, location_redir)
            new_header = True
            req_next = False
            resp_next = False
            url = None
            req_type = None
            resp_code = None
            location_redir = None
    f.close()
            
    return headers



def normalize_url(url):
    if not url or url == '(null)' or url.startswith('about:'):
        return url

    scheme, netloc, path, params, query, fragment = urlparse.urlparse(url)
    path = path.replace("//","/")
    path = path.rstrip("/")
    new_url = urlparse.urlunparse((scheme, netloc, path, params, query, fragment))
    
    return new_url

def remove_anchor_from_url(url):
    if not url:
        return url

    if '#' in url:
        tmp = url.split('#')
        return tmp[0]

    return url



def extract_firefox_event_info_dict(event_type,event_info):
    
    event_info_dict = dict()

    if event_type == "Redirect":
        t = event_info.strip().split()
        # we expect 3 elements 
        # for example TS=1348677231379 URL=http://www.example.com/ REF=http://example.com/
        if len(t)==3:
            event_info_dict['TS'] = int(t[0][3:])
            event_info_dict['URL'] = normalize_url(t[1][4:])
            event_info_dict['REF'] = normalize_url(t[2][4:])

    elif event_type == "PluginRequestURL":
        # we expect 4 elements 
        # for example TS=1348677477163 PN=Shockwave Flash URL=http://example.com/ TAR=_blank
        # print "=====================================>", event_info.strip()
        t = event_info.strip().replace('PN=Shockwave Flash','PN=ShockwaveFlash',1).split()
        if len(t)==4:
            event_info_dict['TS'] = int(t[0][3:])
            event_info_dict['PN'] = t[1][3:]
            event_info_dict['URL'] = normalize_url(t[2][4:])
            event_info_dict['TAR'] = t[3][4:]

    elif event_type == "SetNewURI":
        t = event_info.strip().split()
        # we expect 3 elements 
        # TS=1348677210837 NewURI:http://www.youtube.com/watch?v=zzzz CurrURI:http://www.google.com/xxxx
        if len(t)==3:
            event_info_dict['TS'] = int(t[0][3:])
            event_info_dict['URL'] = normalize_url(t[1][7:])
            event_info_dict['CurrURL'] = normalize_url(t[2][8:])

    elif event_type == "ReplaceURI": 
        t = event_info.strip().split()
        # we expect 3 elements 
        # TS=1348677210837 NewURI:http://www.youtube.com/watch?v=zzzz CurrURI:about:blank
        if len(t)==3:
            event_info_dict['TS'] = int(t[0][3:])
            event_info_dict['URL'] = normalize_url(t[1][7:])
            event_info_dict['CurrURL'] = normalize_url(t[2][8:]) # this turns out to be useless because it's alwasy about:X

    elif event_type == "LoadNewPage":
        t = event_info.strip().split()
        # we expect 3 elements 
        # TS=1348677210754 URL=http://www.google.com/url?sa=t&rct=j&q=wood CurrURL=about:blank REF=http://www.google.com/#hl=en
        if len(t)==4:
            event_info_dict['TS'] = int(t[0][3:])
            event_info_dict['URL'] = normalize_url(t[1][4:])
            event_info_dict['CurrURL'] = normalize_url(t[2][8:])
            event_info_dict['REF'] = normalize_url(t[3][4:])
            
    elif event_type == "ReadFromCache":
        t = event_info.strip().split()
        # we expect 2 elements 
        # TS=1353000399651 URL=http://edge.quantserve.com/quant.js
        if len(t)==2:
            event_info_dict['TS'] = int(t[0][3:])
            event_info_dict['URL'] = normalize_url(t[1][4:])

    if len(event_info_dict) > 0:
        # print event_type, event_info_dict
        return event_info_dict

    return None



def load_firefox_logs(firefox_logs_file):
    
    firefox_logs = dict()
    f = open(firefox_logs_file,'r')
    for l in f.readlines():
        if not l.startswith(":: ClickMiner ::"):
            continue

        tmp = l.split('::')
        if len(tmp) < 4:
            # lines will be of the form ":: ClickMiner :: <event> :: <event_info>"
            continue # something is wrong here, skip

        event = tmp[2].strip()
        if not firefox_logs.has_key(event):
            firefox_logs[event] = []
        event_info = extract_firefox_event_info_dict(event,tmp[3])
        if event_info:
            firefox_logs[event].append(event_info)

    f.close()
    return firefox_logs


def sort_redir_list_by_timestamp(li):
    new_list = []
    ts_list = []
    for r in li:
        ts_list.append(r['TS'])
    sorted_ts_list = sorted(ts_list)
    for s in sorted_ts_list:
        for r in li:
            if r['TS'] <= s:
                new_list.append(r)
    return new_list


def find_redirection_chains_form_firefox_logs(firefox_logs):

    TS_ONE_REDIR_GAP = 2000 # max gap for one single redirection (milliseconds)
    TS_TOTAL_REDIR_GAP = 5000 # max gap for one single redirection (milliseconds)

    replace_redir = []
    explored_redirs = []   
    redir_chains_list = []

    # transform a combination of ReplaceURI+LoadNewPage into a proper redirection
    if firefox_logs.has_key("ReplaceURI"):
        for e1 in firefox_logs["ReplaceURI"]:
            # print "ReplaceURI", e1
            for e2 in firefox_logs["LoadNewPage"]:
                # print "LoadNewPage", e2
                if e2['TS'] > e1['TS']+1 or e1['URL'].startswith('about:'): # the events need to happens basically at the same time
                    break
                if abs(e2['TS']-e1['TS'])<=1 and e2['URL'] == e1['URL'] and e2['CurrURL'] == e1['CurrURL']:
                    r = dict()
                    r['TS'] = e1['TS']
                    r['URL'] = e1['URL']
                    r['REF'] = e2['REF']
                    replace_redir.append(r)
                    # print "===================>", r

    # transform a combination of SetNewURI+LoadNewPage into a proper redirection
    if firefox_logs.has_key("SetNewURI"):
        for e1 in firefox_logs["SetNewURI"]:
            # print "SetNewURI", e1
            for e2 in firefox_logs["LoadNewPage"]:
                # print "LoadNewPage", e2
                if e2['TS'] > e1['TS']+1 or e1['URL'].startswith('about:'): # the events need to happens basically at the same time
                    break
                if abs(e2['TS']-e1['TS'])<=1 and e2['URL'] == e1['URL'] and e2['CurrURL'] == e1['CurrURL']:
                    r = dict()
                    r['TS'] = e1['TS']
                    r['URL'] = e1['URL']
                    r['REF'] = e2['REF']
                    replace_redir.append(r)
                    # print "===================>", r
    
    rlist = replace_redir
    if firefox_logs.has_key("Redirect"):
        rlist = sort_redir_list_by_timestamp(replace_redir+firefox_logs["Redirect"])

    for r1 in rlist:
        if not r1 in explored_redirs and not r1['REF'].startswith('about:'):
            redir_chain = [r1['REF'],r1['URL']]
            explored_redirs.append(r1) # this creates serious problems if the replace_redir and firefox_logs["Redirect"] lists are not sorted by timestamp!
            ts = r1['TS']
            curr_r = r1
            for r2 in rlist:
                if not r2 in explored_redirs:
                    # next redir needs to be within 1s from the frist one (we compare the TS)
                    if r2['REF'] == curr_r['URL'] and r2['TS'] >= curr_r['TS'] and r2['TS'] < curr_r['TS']+TS_ONE_REDIR_GAP and r2['TS'] < r1['TS']+TS_TOTAL_REDIR_GAP:
                        redir_chain.append(r2['URL'])
                        curr_r = r2
                        explored_redirs.append(r2)

            logging.debug("TS = %s, RC = %s" % (ts,redir_chain))
            logging.debug("=========================")
            redir_chains_list.append((ts,redir_chain))

    return redir_chains_list


def is_complete_redirection(orig,dest,redir_chains_list):
    # both orig and dest are tuples: (timestamp, url)

    TS_GAP = 10000 # max gap we allow for a redirection chain (ms)

    (ots,ourl) = orig
    (_,durl) = dest

    # """
    if True:
        logging.debug("CR----------------------------------------")
        logging.debug("ourl = %s" % ourl)
        logging.debug("durl = %s" % durl)
        logging.debug("------------------------------------------")
    # """
    
    for i in range(len(redir_chains_list)):
        r = redir_chains_list[i]
        ts = r[0]
        rc = r[1]

        # """
        # debugging here!
        logging.debug("rc[0] = %s" % rc[0])
        logging.debug("rc[-1] = %s" % rc[-1])
        logging.debug("ts = %s" % ts)
        logging.debug("ots = %s" % ots)
        logging.debug("abs(ts - ots) = %s" % abs(ts - ots))
        # """

        if abs(ts - ots) > TS_GAP:
            continue

        logging.debug("abs(ts - ots) < TS_GAP = %s " % TS_GAP)

        if rc[0] == ourl:
            if rc[-1] == durl:
                return i # return redirection chain element so that it can be deleted by caller

    return None


def is_partial_redirection(orig,dest,redir_chains_list):
    # both orig and dest are tuples: (timestamp, url)

    TS_GAP = 5000 # max gap we allow for a redirection chain (ms)

    (ots,ourl) = orig
    (_,durl) = dest

    """
    if True:
        print "P-----------------------------------------"
        print 'orig',orig
        print 'dest',dest
        print "-----------------------------------------"
    """

    for i in range(len(redir_chains_list)):
        r = redir_chains_list[i]
        ts = r[0]
        rc = r[1]

        if abs(ts - ots) > TS_GAP:
            continue

        """
        if rc[-1] == durl:
            for r in rc:                
                if r == ourl:
                    return True
        """

        # print ts, abs(ts-ots), rc
        if rc[0] == ourl:            
            for u in rc:
                if u == durl:
                    return i

    return None



#def get_redirection_chain_form_http_headers(u,http_headers):
#    chain = [u]
#    # print "Beginning of get_redirection_chain -- chain = %s" % chain
#
#    if not http_headers:
#        return chain
#
#    if len(http_headers) == 0:
#        return chain
#
#    r = None
#    for hdr in http_headers:
#        if hdr['url'] == u:
#            r = hdr['loc_redir']
#            # print "url => redir", u, r
#            break
#
#    if not r:
#        return chain
#
#    # print "Before recursion -- r = %s, chain = %s" % (r,chain)
#    chain.extend(get_redirection_chain(r,http_headers[1:]))
#    # print "After recursion -- r = %s, chain = %s" % (r,chain)
#    return chain




# this does not work well when som of the URLs in the redirection chain
# are retrieved from the cache. In this case LiveHTTPHeaders will
# miss these requests, and therefore we are not going to correctly
# riconstruct the redirection chain
#def find_redirection_chains_form_http_headers(http_headers):
#
#    redir_chains_dict = dict()
#    for i in range(len(http_headers)):
#        u = http_headers[i]['url']
#        redir_chains_dict[u] = None
#
#        rc = get_redirection_chain_form_http_headers(u,http_headers[i:])
#        if len(rc) > 1:
#            redir_chains_dict[rc[-1]] = rc
#            # print "url: %s | rc: %s" % (rc[-1],rc)
#
#    return redir_chains_dict
        
            


# returns index of matching click in pending_clicks
def match_click(p, pending_clicks, redirs):
    if not pending_clicks:
        return None
    if len(pending_clicks) == 0:
        return None

    for i in range(len(pending_clicks))[::-1]:
        pclick = pending_clicks[i]

        logging.debug('pclick.origin.url: %s' % pclick.origin.url) # target url of the pending click
        logging.debug('pclick url: %s' % pclick.getAbsoluteTargetURL()) # target url of the pending click
        logging.debug('pclick.timestamp: %s' % pclick.timestamp) # click time
        logging.debug('p.url: %s' % p.url) # page url
        logging.debug('p.referrer: %s' % p.referrer) # page referrer
        logging.debug('p.timestamp: %s' % p.timestamp) # page time



        # try to encode the urls before attempting to match
        pcatu = pclick.getAbsoluteTargetURL()
        pcou = pclick.origin.url
        purl = p.url 
        pref = p.referrer
     
        if pcatu == purl and pcou == pref:
            logging.debug("match condition: pclick.getAbsoluteTargetURL() == p.url and pclick.origin.url == p.referrer")
            logging.debug("Found match at %s " % i)
            return i

        redir_orig = (pclick.timestamp,pcou)
        redir_dest = (p.timestamp,pref)
        rc_index = is_partial_redirection(redir_orig,redir_dest,redirs)
        if pcatu == purl and not rc_index == None:
            logging.debug("match condition: pclick.getAbsoluteTargetURL() == p.url and is_partial_redirection(redir_orig,redir_dest,redirs)")
            logging.debug("Found match at %s " % i)
            logging.debug("Deleting redirection!")
            del redirs[rc_index]
            return i

        redir_orig = (pclick.timestamp,pcatu)
        redir_dest = (p.timestamp,purl)
        rc_index = is_complete_redirection(redir_orig,redir_dest,redirs)
        if pcou == pref and not rc_index == None:
            logging.debug("match condition: pclick.origin.url == p.referrer and is_complete_redirection(redir_orig,redir_dest,redirs)")
            logging.debug("Found match at %s " % i)
            logging.debug("Deleting redirection!")
            del redirs[rc_index]
            return i

        redir_orig = (pclick.timestamp,pcou)
        redir_dest = (p.timestamp,purl)
        rc_index = is_complete_redirection(redir_orig,redir_dest,redirs)
        if pcatu == pref and not rc_index == None:
            logging.debug("match condition: pclick.origin.url == p.referrer and is_complete_redirection(redir_orig,redir_dest,redirs)")
            logging.debug("Found match at %s " % i)
            logging.debug("Deleting redirection!")
            del redirs[rc_index]
            return i

        redir_orig = (pclick.timestamp,pcou)
        redir_dest = (p.timestamp,purl)
        rc_index = is_complete_redirection(redir_orig,redir_dest,redirs)
        if pcatu == Click.JS_H and not rc_index == None:
            logging.debug("match condition: pclick.origin.url == p.referrer and is_complete_redirection(redir_orig,redir_dest,redirs)")
            logging.debug("Found match at %s " % i)
            logging.debug("Deleting redirection!")
            del redirs[rc_index]
            return i

        redir_orig = (pclick.timestamp,pcatu)
        redir_dest = (p.timestamp,purl)
        rc_index = is_complete_redirection(redir_orig,redir_dest,redirs)
        if rc_index:
            logging.debug("match condition: is_complete_redirection(redir_orig,redir_dest,redirs)")
            logging.debug("Found match at %s " % i)
            logging.debug("Deleting redirection!")
            del redirs[rc_index]
            return i

        redir_orig = (pclick.timestamp,pcatu)
        redir_dest = (p.timestamp,purl)
        if is_partial_redirection(redir_orig,redir_dest,redirs) and pcou == pref:
            logging.debug("match condition: is_partial_redirection(redir_orig,redir_dest,redirs) and pclick.origin.url == p.referrer")
            logging.debug("Found match at %s " % i)
            return i 

            """
            if not len(pending_clicks_index) >= 0:
                # if none can be found, try to match only the referrer
                # giving precedence to clicks on embedded objects
                click_match_conditions = '(pclick.getAbsoluteTargetURL() == Click.JS_EMBEDDED_H or pclick.getAbsoluteTargetURL() == Click.EMBEDDED_H) and pclick.origin.url == p.referrer'
                pending_clicks_index = match_click(p, pending_clicks, click_match_conditions)

            if not pending_clicks_index >= 0:
                # if none can be found, try to match only the referrer
                # in cases when the click could be handled by the javascript engine
                click_match_conditions = 'pclick.getAbsoluteTargetURL() == Click.JS_H and pclick.origin.url == p.referrer'
                pending_clicks_index = match_click(p, pending_clicks, click_match_conditions)
            """

    return None


def satisfied_from_cache(c,firefox_logs):
    
    TS_CLICK_GAP = 1000
    
    if not firefox_logs.has_key("ReadFromCache"):
        return False
    cache_reads = firefox_logs["ReadFromCache"]
    
    for cr in cache_reads:
        if abs(cr['TS'] - c.timestamp) < TS_CLICK_GAP and \
            cr['URL'] == c.getAbsoluteTargetURL():
            return True
        
    return False

def find_plugin_click(c,firefox_logs):

    TS_CLICK_GAP = 1000
    TS_LOAD_GAP  = 3000

    # print "PN: Looking for a plugin click!"
    
    if not firefox_logs.has_key("PluginRequestURL"):
        return None
    plugin_clicks = firefox_logs["PluginRequestURL"]              
    
    if not firefox_logs.has_key("LoadNewPage"):
        return None
    page_loads = firefox_logs["LoadNewPage"]
    
    for pc in plugin_clicks:
        if abs(pc['TS'] - c.timestamp) < TS_CLICK_GAP:
            # print "PN: Candidate Click:", pc['URL']
            click_ref = None
            for pl in page_loads:
                # print "PN: pl:", (pl['URL'] == pc['URL']), (pl['TS'] > pc['TS']), (pl['TS'] - pc['TS'] < TS_GAP)
                # print "PN:", pl['TS'], pc['TS']
                if pl['URL'] == pc['URL'] and pl['TS'] > pc['TS'] and pl['TS'] - pc['TS'] < TS_LOAD_GAP:
                    click_ref = pl['REF']
                    # print "PN: Click REF:", pc['URL']
            if click_ref == c.origin.url:
                # print "PN: Foudn Click:", pc['URL']
                return pc['URL']

    return None
            



def process_logs(events_file,firefox_logs_file):
    events = load_events(events_file)
    # http_headers = load_http_headers(headers_file)
    # redirections = find_redirection_chains_form_http_headers(http_headers)
    firefox_logs = load_firefox_logs(firefox_logs_file)
    redirections = find_redirection_chains_form_firefox_logs(firefox_logs)

    print 'Number of selenium ide events found:', len(events)
    for key in firefox_logs.keys():
        print 'Number of firefox', key, 'events found:', len(firefox_logs[key])

    curr_win_ids = []
    curr_win_frame_list = dict()
    curr_pages = dict()
    pending_clicks = []
    recorded_clicks = []
    confirmed_clicks = []

    for e in events:
        new_page = False

        # build window object from event info
        w = Window()
        f = None
        w.h_len = e.history_len 
        w.opener = e.opener
        if not e.within_frame: # event is in main window
            w.id = e.id
        else: # even is in a frame
            w.id = e.url_path[0][0]
            
            # build frame object, if any
            f = Frame()
            f.id = e.id
            f.w_id = w.id
            for i in range(1,len(e.url_path)):
                f.path.append((e.url_path[i][0],e.url_path[i][1]))
                f.path

        wh = w.hash()
        if not wh in curr_win_frame_list:
            curr_win_frame_list[wh] = w

        if f:
            fh = f.hash()
            if not fh in curr_win_frame_list:
                curr_win_frame_list[fh] = f

        # build page object
        p = Page()
        p.window = w
        p.frame = f
        p.title = e.title
        p.url = normalize_url(e.url_path[-1][1])
        p.referrer = normalize_url(e.referrer)
        p.timestamp = e.time
        logging.debug("current page: %s" % p)
        logging.debug("event URL path: %s" % e.url_path)
        
        # if we already had this page,
        # retrieve previous copy and update timestamp
        ph = p.hash()
        if not ph in curr_pages:
            curr_pages[ph] = p
            new_page = True

        if curr_pages[ph].timestamp < e.time: # update timestamp if needed
            curr_pages[ph].title = e.title # the title page may change after loading (may be null until load is complete)   
            curr_pages[ph].timestamp = e.time 
            logging.debug("Upadated page title and timestamp")

        if new_page:
            logging.debug("========= New Page =========")
            if not len(pending_clicks) > 0:                
                logging.debug("no pending clicks!")
            else:
                logging.debug("looking for related click (if any can be found)")

            pending_clicks_index = None

            # first try to find a click that matches both target url and referrer
            pending_clicks_index = match_click(p, pending_clicks, redirections) 

            if pending_clicks_index >= 0:
                pclick = pending_clicks[pending_clicks_index]
                pclick.destination = p
                confirmed_clicks.append(pclick)
                del pending_clicks[pending_clicks_index]
                logging.debug("Found new confirmed click: %s" % pclick)
                print ''
                print "=== Found new confirmed click ==="
                print "Click origin page url: %s" % pclick.origin.url
                print "Click origin page tile: %s" % pclick.origin.title
                print "Click target url: %s" % pclick.getAbsoluteTargetURL()
                print "Click destination page url: %s" % pclick.destination.url
                print "Click destination page title: %s" % pclick.destination.title
                print "Click timestamp: %s" % pclick.timestamp
                print "================================="

            # elif not p.referrer and not p.window.opener:
            if True: # print information about the page even if was not possible to link it to a click
                print ''
                if pending_clicks_index >= 0: # click has been confirmed
                    if not p.frame:
                        print "=== Found new page (through a click) ==="
                    else:
                        print "=== Found new frame page ==="
                elif p.window.h_len <= 1 and not p.frame:
                    print "=== Found new page (no click, new tab/window) ==="
                elif p.window.h_len <= 2 and p.window.id not in curr_win_ids and not p.frame:
                    # p.window.h_len <= 2 is due to the fact that a new empty tab/window
                    # counts towards 1 in the window history length
                    print "=== Found new page (no click, likely new tab/window) ==="
                elif not p.frame:
                    print "=== Found new page (no click, likely typed on url bar) ==="
                elif p.frame:
                    print "=== Found new frame page (no click) ==="
                print "Page window ID: %s" % p.window.id
                if p.frame:
                    print "Page frame ID: %s" % p.frame.id
                print "Page url: %s" % p.url
                print "Page referrer: %s" % p.referrer
                print "Page title: %s" % p.title
                print "Page timestamp: %s" % p.timestamp
                print "================================="

            # curr_win_ids are used only to (approximately) keep track of pages opened on new tabs/windows
            if p.window.id not in curr_win_ids:
                curr_win_ids.append(p.window.id)

        logging.debug("Event type: %s " % e.type)
        logging.debug("Event page url: %s " % e.url)
        logging.debug("Event timestamp: %s " % e.time)


        # TODO: we need to also add handling of contexmenu
        # noticed that pressing [Enter] in a form actually results in a click
        # if e.type == 'mousedown': # covers the clicks as well as contextmenu
        if e.type == 'click' or e.type == 'contextmenu': # covers the clicks as well as contextmenu
            logging.debug("Processing click (%s)" % e.type)
            # build click object        
            c = Click()
            c.origin = p
            c.destination = None
            c.handlers = e.click_handlers
            c.target_url = normalize_url(e.target_url)
            c.target_object = e.target_object
            c.target_locator = e.locator_path
            c.timestamp = e.time
            c.load_from_cache = False

            logging.debug("click: %s" % c)

            # check if any target URL can be found (even if it could be '[unknown]')
            if c.getAbsoluteTargetURL():
                c.load_from_cache = satisfied_from_cache(c, firefox_logs)

                # if this was a click on a plugin, try to see if we can get an actual target URL
                if c.getAbsoluteTargetURL() == Click.EMBEDDED_H:
                    plugin_req = find_plugin_click(c, firefox_logs)
                    if plugin_req:
                        c.target_url = plugin_req

                if len(recorded_clicks) == 0 or (len(recorded_clicks) > 0 and not str(c) == str(recorded_clicks[-1])): # avoids problems with cosecutive "duplicate" clicks
                    recorded_clicks.append(c)
                    if len(pending_clicks) == 0:
                        pending_clicks.append(c) # add c to the list of clicks that have not yet been matched with a new page
                        logging.debug("Added click to pending_clicks list")
                    elif not str(c) == str(pending_clicks[-1]):
                        pending_clicks.append(c) # add c to the list of clicks that have not yet been matched with a new page
                        logging.debug("Added click to pending_clicks list")

                print ''
                print "=== Found new click ==="
                print "Click origin window: %s" % c.origin.window.id
                if c.origin.frame:
                    print "Click origin frame: %s" % c.origin.frame.id
                print "Click origin page url: %s" % c.origin.url
                print "Click origin page tile: %s" % c.origin.title
                print "Click target url: %s" % c.getAbsoluteTargetURL()
                print "Click locator path: %s" % c.target_locator
                print "Click timestamp: %s" % c.timestamp
                print "======================="

    for c in recorded_clicks:
        if c in confirmed_clicks:
            c.confirmed = True

    return recorded_clicks


def export_json(clicks, path):
    jsonarr = []
    for click in clicks:
        jsonarr.append(click.state())
    
    out = open(path, 'w')
    out.write(json.dumps(jsonarr, indent=4))
    out.close()
    
def import_json(path):
    retval = []
    infile = open(path, 'r')
    jsonstr = ""
    for line in infile:
        jsonstr += line
        
    jsonarr = json.loads(jsonstr)
    for s in jsonarr:
        c = Click()
        c.from_state(s)
        retval.append(c)
        
    return retval
        

def test1():        
    export_json(process_logs(), sys.argv[1] + ".json.log")
    
def test2():
    clicks1 = process_logs()
    export_json(clicks1, sys.argv[1] + ".json.log")
    clicks2 = import_json(sys.argv[1] + ".json.log")
    
    if len(clicks1) == len(clicks2):
        for i in range(len(clicks1)):
            if str(clicks1[i]) == str(clicks2[i]):
                print "Click objects at index " + str(i) +  " match."
            else:
                print "Click objects at index " + str(i) +  " do not match."
                
    else:
        print "Wrong length for deserialized click array."
    

if __name__ == "__main__":
    if len(sys.argv) < 4:
        print "Usage: %s <events_file> <firefox_log_file> <output_file> " % sys.argv[0]
        sys.exit(1)

    events_file = sys.argv[1]
    firefox_logs_file = sys.argv[2]
    # headers_file = sys.argv[3]
    clicks = process_logs(events_file,firefox_logs_file)
    export_json(clicks, sys.argv[3])
    #test1()
    #test2()

