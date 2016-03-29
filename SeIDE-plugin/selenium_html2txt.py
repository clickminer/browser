#!/usr/bin/python

import sys
from HTMLParser import HTMLParser

class MyHTMLParser(HTMLParser):

    new_event_log = False
    new_action = False
    new_action_content = False # needed to handle content on multiple lines
    action_content = ''

    printable_info = ['Event Time','Event Type','Window ID','Window URL','Window Path','Window Opener','Window Referrer','Window Title','Window History Length','Frame ID','Frame Path','Frame Referrer','Frame Title','Click Handlers','Element Locator Path','Target']

    def handle_starttag(self, tag, attrs):
        if tag == 'td':
            self.new_action = True
            self.new_action_content = False
            
    def handle_endtag(self, tag):
        if tag == 'td':
            if self.new_action_content and not len(self.action_content) == 0:
                print self.action_content
            self.new_action = False
            self.new_action_content = False

    def handle_entityref(self, name):
        # c = unichr(name2codepoint[name])
        html_escape_dict = {
            'lt'   : '<',
            'gt'   : '>',
            'amp'  : '&',
            'quot' : '"',
            'apos' : '\'',
            'nbsp' : ' '
        }
        self.action_content += html_escape_dict[name]

    def handle_data(self, data):
        if not self.new_action:
            return
        if not self.new_action_content and not data.startswith('::'):
            return
        if not self.new_event_log and data.startswith(':: Event Time :: '):
            # the first row related to a new event must start with the event time
            # print "Starting new event!!!", data
            self.new_event_log = True

        if not self.new_event_log:
            return

        if not self.new_action_content and data.startswith('::'):
            info_type = data.split("::")[1].strip()
            if info_type in self.printable_info:
                self.new_action_content = True
                self.action_content = ''

        if self.new_action_content: 
            self.action_content += data

        if self.new_event_log and data.startswith(':: Target :: '):
            # the last row related to an even always ends with Target
            self.new_event_log = False

def main():
    filename = sys.argv[1]
    f = open(filename,'rb')
    parser = MyHTMLParser()
    parser.feed(f.read())
    f.close()


if __name__ == "__main__":
    main()
