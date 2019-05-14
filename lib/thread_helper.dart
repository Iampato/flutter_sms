import 'package:sms_maintained/sms.dart';
import 'package:sms_maintained/contact.dart';
import 'dart:collection';
import 'package:observable/observable.dart';
import 'package:observable_ish/observable_ish.dart';

/*
* this class is meant to make it more easy to interact with the other classes
* you don't have to use this to make use of contact.dart and sms.dart
* */
class ThreadHelper {
  RxList<SmsThread> threads;
  static ThreadHelper _threadHelper;
  SmsReceiver _receiver;

  ThreadHelper(){}

  static Future<ThreadHelper> getObject() async {
    if(_threadHelper == null){
      _threadHelper = ThreadHelper();
      await _threadHelper.loadEverything();
      return _threadHelper;
    }
    else {
      return _threadHelper;
    }
  }



  /*
  * Do not forget to have the right permissions or else this is going to fail big time.
  * */
  Future<void> loadEverything() async{
    SmsQuery query = new SmsQuery();
    List<SmsThread> allThreadsNormalList = await query.getAllThreads;
    this.threads.addAll(allThreadsNormalList);
    this.threads = RxList<SmsThread>();
  }


  Future refreshThreads() async {
    SmsQuery query = new SmsQuery();
    List<SmsThread> allThreadsNormalList = await query.getAllThreads;
    threads.clear();
    for(SmsThread smsThread in allThreadsNormalList) {
      this.threads.add(smsThread);
    }
  }

  /*
  * TODO : load only a certain thread with an id
  * */
  Future refreshThreadWithId() async {
    throw new Exception("not yet implemented");
  }

  setReceiver(){
    this._receiver = SmsReceiver();
    this._receiver.onSmsReceived.listen((SmsMessage message) async {
      SmsQuery query = new SmsQuery();
      SmsThread thread = (await query.queryThreads([message.threadId])).first;
      thread.sortMessages(sort: SmsSort.UP);
      this.updateThreadWithThread(thread);
    });
  }

  updateThreadWithThread(SmsThread thread) {
    int indexThread = this.threads.indexWhere((x) => x.threadId == thread.threadId);
    if(indexThread != 0){
      this.threads.removeAt(indexThread);
      this.threads.add(thread);
    }
    return thread;
  }

  Future<SmsThread> updateThreadWithId(int id) async {
    SmsQuery query = new SmsQuery();
    SmsThread thread = (await query.queryThreads([id])).first;
    return updateThreadWithThread(thread);
  }




  



}