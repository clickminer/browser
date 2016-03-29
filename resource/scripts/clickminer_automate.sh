#!/bin/bash -x
CLICKMINDER_DIR=/home/cjneasbi/clickminer
WORKSPACE_DIR=/home/cjneasbi/workspace
TRACES_DIR=$CLICKMINDER_DIR/test_traces
TRACE_PCAP=traffic_trace.pcap
TRACE_FLOW=traffic_trace.flow

NATIVE_LIB_PATH=$CLICKMINER_DIR/pcap-reconst/lib/x64

#BROWSER_DIR=$CLICKMINDER_DIR/clickminer-browser-0.5
BROWSER_DIR=$WORKSPACE_DIR/clickminer-browser
BROWSER_PROFILE=/home/cjneasbi/.mozilla/firefox/$(ls /home/cjneasbi/.mozilla/firefox | grep webdriver)
#BROWSER_PROFILE=/home/cjneasbi/.mozilla/firefox/qpmj5339.webdriver
#BROWSER_BINARY=/home/cjneasbi/Desktop/old_firefox/firefox-14.0.1/firefox-bin
BROWSER_BINARY=/home/cjneasbi/Desktop/old_firefox/firefox-17.0/firefox-bin
#BROWSER_BINARY=/home/cjneasbi/Desktop/old_firefox/firefox-14.0.1.source-instrumented/mozilla-release-instrumented/obj-x86_64-unknown-linux-gnu/dist/bin/firefox
BROWSER_OUTPUT=mined_clicks.json
BROWSER_LOG=clickminer_browser.log
BROWSER_JAR=clickminer-browser-0.1-executable.jar

PROXY_DIR=$WORKSPACE_DIR/mitmproxy
#INSTSERVER_DIR=$CLICKMINDER_DIR/clickminer-proxy-0.5
INSTSERVER_DIR=$WORKSPACE_DIR/clickminer-proxy
PROXY_PID=0
PROXY_LOG=clickminer_proxy.log
PROXY_PYTHONPATH=$WORKSPACE_DIR/mitmproxy:$WORKSPACE_DIR/netlib:/usr/local/lib/python2.7/dist-packages/python_graph_core-1.8.1-py2.7.egg:/usr/local/lib/python2.7/dist-packages/python_graph_dot-1.8.1-py2.7.egg:/usr/local/lib/python2.7/dist-packages:/usr/lib/python2.7:/usr/lib/python2.7/plat-linux2:/usr/lib/python2.7/lib-tk:/usr/lib/python2.7/lib-old:/usr/lib/python2.7/lib-dynload:/usr/local/lib/python2.7/dist-packages:/usr/lib/python2.7/dist-packages:/usr/lib/python2.7/dist-packages/PIL:/usr/lib/python2.7/dist-packages/gst-0.10:/usr/lib/python2.7/dist-packages/gtk-2.0:/usr/lib/pymodules/python2.7

EXTRACT_DIR=$WORKSPACE_DIR/mitmextract
EXTRACT_LOG=mitmextract.log
EXTRACT_PYTHONPATH=$WORKSPACE_DIR/mitmextract:$WORKSPACE_DIR/netlib:$WORKSPACE_DIR/mitmproxy:/usr/local/lib/python2.7/dist-packages/python_graph_core-1.8.1-py2.7.egg:/usr/local/lib/python2.7/dist-packages/python_graph_dot-1.8.1-py2.7.egg:/usr/local/lib/python2.7/dist-packages:/usr/lib/python2.7:/usr/lib/python2.7/plat-linux2:/usr/lib/python2.7/lib-tk:/usr/lib/python2.7/lib-old:/usr/lib/python2.7/lib-dynload:/usr/local/lib/python2.7/dist-packages:/usr/lib/python2.7/dist-packages:/usr/lib/python2.7/dist-packages/PIL:/usr/lib/python2.7/dist-packages/gst-0.10:/usr/lib/python2.7/dist-packages/gtk-2.0:/usr/lib/pymodules/python2.7

PARSE_OUTPUT=recorded_clicks.json
PARSE_LOG=parse_selenium_txt_logs.log
RESULTS_LOG=clickminer_results.log
RESULTS_OUTPUT=results_comparison.txt


for td in $(ls $TRACES_DIR | grep -E -e "user[0-9][0-9]*_(no)?cache")
do
	DEST_DIR=$TRACES_DIR/$td	

    #extract the flows from the pcap trace
	cd $EXTRACT_DIR
	PYTHONPATH=$EXTRACT_PYTHONPATH /usr/bin/python2.7 -u mitmextract.py -d -f Content-Type:application/ocsp-request $DEST_DIR/$TRACE_PCAP $DEST_DIR/$TRACE_FLOW &> $DEST_DIR/$EXTRACT_LOG

    #start the proxy
	cd $PROXY_DIR
	PYTHONPATH=$PROXY_PYTHONPATH /usr/bin/python2.7 -u ./mitmdump --norefresh -e -s $INSTSERVER_DIR/clickminer/instrument_server.py -k --keepserving -S $DEST_DIR/$TRACE_FLOW &> $DEST_DIR/$PROXY_LOG &
	PROXY_PID=$!

    #gives time for the proxy server to start
    sleep 5

    #start the browser, make sure to create the executable jar
	cd $BROWSER_DIR/target
	java -Xms512m -Xmx1024m -jar $BROWSER_JAR -b $BROWSER_BINARY -h 127.0.0.1 -P $BROWSER_PROFILE -p 8080 -q 8888 -l 3 -j -f text/css -f text/javascript -f application/x-javascript -f application/javascript \ 
		-f application/x-shockwave-flash -o $DEST_DIR/$BROWSER_OUTPUT &> $DEST_DIR/$BROWSER_LOG

    #kill the proxy
    kill -9 $PROXY_PID &> /dev/null

    #parse the recorded clicks
    cd $BROWSER_DIR/SeIDE-plugin
    /usr/bin/python2.7 selenium_html2txt.py $DEST_DIR/seleniumide_log.html > $DEST_DIR/seleniumide_log.txt
    /usr/bin/python2.7 parse_selenium_txt_logs.py $DEST_DIR/seleniumide_log.txt $DEST_DIR/firefox_log.txt $DEST_DIR/$PARSE_OUTPUT &> $DEST_DIR/$PARSE_LOG
    
    #compare the minded and recorded clicks
    cd $BROWSER_DIR/target
    java -Djava.library.path=$NATIVE_LIB_PATH -cp $BROWSER_JAR edu.uga.cs.clickminer.results.ClickminerResultsCLI -m $DEST_DIR/$BROWSER_OUTPUT -r $DEST_DIR/$PARSE_OUTPUT \ 
    	-v $DEST_DIR/$TRACE_PCAP -f -a -o $DEST_DIR 2> $DEST_DIR/$RESULTS_LOG
    	
    #plot a comparison between clickminer results and the naive comparison algorithm
	java -Djava.library.path=$NATIVE_LIB_PATH -cp $BROWSER_JAR edu.uga.cs.clickminer.results.ClickminerPlotterCLI -m $DEST_DIR/$BROWSER_OUTPUT -r $DEST_DIR/$PARSE_OUTPUT \ 
    	-v $DEST_DIR/$TRACE_PCAP -f -a -o $DEST_DIR 2> $DEST_DIR/$RESULTS_LOG
        
done
