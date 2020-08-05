/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.unimelb.marinig.bletracker.Logger;

/**
 * So the idea behind the LogNode is that the Log (TrackerLog) will send out the text to be printed
 * The first to get the data is the first node set through TrackerLog.setLogNode, after this node printed
 * It will sent the info to the next nodes, it may also filter up some info so that all the next nodes in the chain won't get unnecessary info
 * The next nodes will then print or do whatever and then sent out the data to the next nodes in the chain!
 *
 * All the nodes must implement the LogNode interface which has the println method, which in theory should call the println method
 * of the next node in the chain calling something like mNextNode.println(...)
 * So in theory every node should also allow setNextNode() and getNextNode()
 */

/**
 * Basic interface for a logging system that can output to one or more targets.
 * Note that in addition to classes that will output these logs in some format,
 * one can also implement this interface over a filter and insert that in the chain,
 * such that no targets further down see certain data, or see manipulated forms of the data.
 * You could, for instance, write a "ToHtmlLoggerNode" that just converted all the log data
 * it received to HTML and sent it along to the next node in the chain, without printing it
 * anywhere.
 */
public interface LogNode {

    /**
     * Instructs first LogNode in the list to print the log data provided.
     * @param priority Log level of the data being logged.  Verbose, Error, etc.
     * @param tag Tag for for the log data.  Can be used to organize log statements.
     * @param msg The actual message to be logged. The actual message to be logged.
     * @param tr If an exception was thrown, this can be sent along for the logging facilities
     *           to extract and print useful information.
     */
    public void println(int priority, String date, String tag, String msg, Throwable tr);
    public void setNext(LogNode node);
    public LogNode getNext();

}
