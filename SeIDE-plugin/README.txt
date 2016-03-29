running Selenium IDE logs parser:

First translate html logs into txt format:

./selenium_html2txt.py example-trace.html > example-trace.txt

then, run the parser:

python parse_selenium_txt_logs.py example-trace.txt firefox_log.txt output.json
