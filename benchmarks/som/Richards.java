package som;

import java.util.Arrays;

/*
 * This version is a port of the SOM Richards benchmark to Java.
 * It is kept as close to the SOM version as possible, for cross-language
 * benchmarking.
 */

public class Richards extends Benchmark {

  public static void main(final String[] args) {
    new Richards().run(args);
  }

  @Override
  public Object benchmark() {
    RBObject.initialize();
    (new RichardsBenchmarks()).reBenchStart();
    return null;
  }

  @FunctionalInterface
  public interface ProcessFunction {
    TaskControlBlock apply(final Packet work, final RBObject word);
  }

  public static class RichardsBenchmarks extends RBObject {
    private TaskControlBlock taskList;
    private TaskControlBlock currentTask;
    private int currentTaskIdentity;
    private TaskControlBlock[] taskTable;
    private boolean tracing;
    private int layout;
    private int queuePacketCount;
    private int holdCount;

    void createDevice(final int identity, final int priority,
        final Packet workPacket, final TaskState state) {
      DeviceTaskDataRecord data = DeviceTaskDataRecord.create();

      createTask(identity, priority, workPacket, state,
         (final Packet workArg, final RBObject wordArg) -> {
           DeviceTaskDataRecord dataRecord = (DeviceTaskDataRecord) wordArg;
           Packet functionWork = workArg;
           if (RBObject.noWork() == functionWork) {
             if (RBObject.noWork() == (functionWork = dataRecord.getPending())) {
               return markWaiting();
             } else {
               dataRecord.setPending(RBObject.noWork());
               return queuePacket(functionWork);
             }
           } else {
             dataRecord.setPending(functionWork);
             if (tracing) {
               trace(functionWork.getDatum());
             }
             return holdSelf();
           }},
        data);
    }

    void createHandler(final int identity, final int priority,
        final Packet workPaket, final TaskState state) {
      HandlerTaskDataRecord data = HandlerTaskDataRecord.create();
      createTask(identity, priority, workPaket, state,
          (final Packet work, final RBObject word) -> {
            HandlerTaskDataRecord dataRecord = (HandlerTaskDataRecord) word;
            if (RBObject.noWork() != work) {
              if (RBObject.workPacketKind() == work.getKind()) {
                dataRecord.workInAdd(work);
              } else {
                dataRecord.deviceInAdd(work);
              }
            }

            Packet workPacket;
            if (RBObject.noWork() == (workPacket = dataRecord.workIn())) {
              return markWaiting();
            } else {
              int count = workPacket.getDatum();
              if (count > 4) {
                dataRecord.workIn(workPacket.getLink());
                return queuePacket(workPacket);
              } else {
                Packet devicePacket;
                if (RBObject.noWork() == (devicePacket = dataRecord.deviceIn())) {
                  return markWaiting();
                } else {
                  dataRecord.deviceIn(devicePacket.getLink());
                  devicePacket.setDatum(workPacket.getData()[count - 1]);  // -1 for Java indexing????
                  workPacket.setDatum(count + 1);
                  return queuePacket(devicePacket);
                }
              }
            }
        }, data);
    }

    void createIdler(final int identity, final int priority, final Packet work,
        final TaskState state) {
          IdleTaskDataRecord data = IdleTaskDataRecord.create();
          createTask(identity, priority, work, state,
              (final Packet workArg, final RBObject wordArg) -> {
                IdleTaskDataRecord dataRecord = (IdleTaskDataRecord) wordArg;
                dataRecord.setCount(dataRecord.getCount() - 1);
                if (0 == dataRecord.getCount()) {
                  return holdSelf();
                } else {
                  if (0 == (dataRecord.getControl() & 1)) {
                    dataRecord.setControl(dataRecord.getControl() / 2);
                    return release(RBObject.deviceA());
                  } else {
                    dataRecord.setControl((dataRecord.getControl() / 2) ^ 53256);
                    return release(RBObject.deviceB());
                  }
                }
              }, data);
    }

    Packet createPacket(final Packet link, final int identity, final int kind) {
      return Packet.create(link, identity, kind);
    }

    void createTask(final int identity, final int priority,
        final Packet work, final TaskState state,
        final ProcessFunction aBlock, final RBObject data) {

      TaskControlBlock t = TaskControlBlock.create(taskList, identity,
          priority, work, state, aBlock, data);
      taskList = t;
      taskTable[identity - 1] = t;  // Java indexing -1
    }

    void createWorker(final int identity, final int priority,
        final Packet workPaket, final TaskState state) {
      WorkerTaskDataRecord dataRecord = WorkerTaskDataRecord.create();
      createTask(identity, priority, workPaket, state,

          (final Packet work, final RBObject word) -> {
            WorkerTaskDataRecord data = (WorkerTaskDataRecord) word;
            if (RBObject.noWork() == work) {
              return markWaiting();
            } else {
              data.setDestination(
                  (RBObject.handlerA() == data.getDestination()) ?
                      RBObject.handlerB() : RBObject.handlerA());
              work.setIdentity(data.getDestination());
              work.setDatum(1);
              for (int i = 0; i < 4; i++) {
                data.setCount(data.getCount() + 1);
                if (data.getCount() > 26) { data.setCount(1); }
                work.getData()[i] = 65 + data.getCount() - 1;
              }
              return queuePacket(work);
            }
          }, dataRecord);
    }

    void reBenchStart() {
      Packet workQ;
      initTrace();
      initScheduler();

      createIdler(RBObject.idler(), 0, RBObject.noWork(),
          TaskState.createRunning());
      workQ = createPacket(RBObject.noWork(), RBObject.worker(),
          RBObject.workPacketKind());
      workQ = createPacket(workQ, RBObject.worker(),
          RBObject.workPacketKind());

      createWorker(RBObject.worker(), 1000, workQ,
          TaskState.createWaitingWithPacket());
      workQ = createPacket(RBObject.noWork(), RBObject.deviceA(),
          RBObject.devicePacketKind());
      workQ = createPacket(workQ, RBObject.deviceA(), RBObject.devicePacketKind());
      workQ = createPacket(workQ, RBObject.deviceA(), RBObject.devicePacketKind());

      createHandler(RBObject.handlerA(), 2000, workQ,
          TaskState.createWaitingWithPacket());
      workQ = createPacket(RBObject.noWork(), RBObject.deviceB(),
          RBObject.devicePacketKind());
      workQ = createPacket(workQ, RBObject.deviceB(), RBObject.devicePacketKind());
      workQ = createPacket(workQ, RBObject.deviceB(), RBObject.devicePacketKind());

      createHandler(RBObject.handlerB(), 3000, workQ,
          TaskState.createWaitingWithPacket());
      createDevice(RBObject.deviceA(), 4000, RBObject.noWork(),
          TaskState.createWaiting());
      createDevice(RBObject.deviceB(), 5000, RBObject.noWork(),
          TaskState.createWaiting());

      schedule();

      if (queuePacketCount != 23246 || holdCount != 9297) {
        throw new RuntimeException("Results are incorrect");
      }
    }

    TaskControlBlock findTask(final int identity) {
      TaskControlBlock t = taskTable[identity - 1]; // java indexing -1
      if (RBObject.noTask() == t) { throw new RuntimeException("findTask failed"); }
      return t;
    }

    TaskControlBlock holdSelf() {
      holdCount = holdCount + 1;
      currentTask.setTaskHolding(true);
      return currentTask.getLink();
    }

    void initScheduler() {
      queuePacketCount = 0;
      holdCount = 0;
      taskTable = new TaskControlBlock[6];
      Arrays.setAll(taskTable, v -> RBObject.noTask());
      taskList = RBObject.noTask();
    }

    void initTrace() {
      tracing = false;
      layout  = 0;
    }

    TaskControlBlock queuePacket(final Packet packet) {
      TaskControlBlock t = findTask(packet.getIdentity());
      if (RBObject.noTask() == t) { return RBObject.noTask(); }

      queuePacketCount = queuePacketCount + 1;

      packet.setLink(RBObject.noWork());
      packet.setIdentity(currentTaskIdentity);
      return t.addInputAndCheckPriority(packet, currentTask);
    }

    TaskControlBlock release(final int identity) {
      TaskControlBlock t = findTask(identity);
      if (RBObject.noTask() == t) { return RBObject.noTask(); }
      t.setTaskHolding(false);
      if (t.getPriority() > currentTask.getPriority()) {
        return t;
      } else {
        return currentTask;
      }
    }

    void trace(final int id) {
      layout = layout - 1;
      if (0 >= layout) {
        Transcript.cr();
        layout = 50;
      }
      Transcript.show("" + id);
    }

    TaskControlBlock markWaiting() {
      currentTask.setTaskWaiting(true);
      return currentTask;
    }

    void schedule() {
      currentTask = taskList;
      while (RBObject.noTask() != currentTask) {
        if (currentTask.isTaskHoldingOrWaiting()) {
          currentTask = currentTask.getLink();
        } else {
          currentTaskIdentity = currentTask.getIdentity();
          if (tracing) { trace(currentTaskIdentity); }
          currentTask = currentTask.runTask();
        }
      }
    }
  }

  public static class RBObject {
    public Packet append(final Packet packet, final Packet queueHead) {
      packet.setLink(RBObject.noWork());
      if (RBObject.noWork() == queueHead) {
        return packet;
      }

      Packet mouse = queueHead;
      Packet link;
      while (RBObject.noWork() != (link = mouse.getLink())) {
        mouse = link;
      }
      mouse.setLink(packet);
      return queueHead;
    }

    private static int    DeviceA;
    private static int    DeviceB;
    private static int    DevicePacketKind;
    private static int    HandlerA;
    private static int    HandlerB;
    private static int    Idler;
    private static TaskControlBlock NoTask;
    private static Packet NoWork;
    private static int    Worker;
    private static int    WorkPacketKind;

    public static TaskControlBlock noTask() { return NoTask; }
    public static int    idler()  { return Idler;  }
    public static Packet noWork() { return NoWork; }
    public static int    worker() { return Worker; }
    public static int    workPacketKind() { return WorkPacketKind; }
    public static int    handlerA() { return HandlerA; }
    public static int    handlerB() { return HandlerB; }
    public static int    deviceA()  { return DeviceA;  }
    public static int    deviceB()  { return DeviceB;  }
    public static int    devicePacketKind() { return DevicePacketKind; }

    public static void initialize() {
      DeviceA          = 5;
      DeviceB          = 6;
      DevicePacketKind = 1;
      HandlerA         = 3;
      HandlerB         = 4;
      Idler            = 1;
      NoWork           = null;
      NoTask           = null;
      Worker           = 2;
      WorkPacketKind   = 2;
    }
  }

  public static class HandlerTaskDataRecord extends RBObject {
    private Packet workIn;
    private Packet deviceIn;

    public Packet deviceIn() { return deviceIn; }
    public void deviceIn(final Packet aPacket) { deviceIn = aPacket; }

    public void deviceInAdd(final Packet packet) {
      deviceIn = append(packet, deviceIn);
    }

    public Packet workIn() { return workIn; }
    public void workIn(final Packet aWorkQueue) { workIn = aWorkQueue; }

    public void workInAdd(final Packet packet) {
      workIn = append(packet, workIn);
    }

    private HandlerTaskDataRecord() {
      workIn = deviceIn = RBObject.noWork();
    }

    public static HandlerTaskDataRecord create() {
      return new HandlerTaskDataRecord();
    }
  }

  public static class IdleTaskDataRecord extends RBObject {
    private int control;
    private int count;

    public int getControl() { return control; }
    public void setControl(final int aNumber) {
      control = aNumber;
    }

    public int getCount() { return count; }
    public void setCount(final int aCount) { count = aCount; }

    public IdleTaskDataRecord() {
      control = 1;
      count = 10000;
    }

    public static IdleTaskDataRecord create() { return new IdleTaskDataRecord(); }
  }

  public static class Packet extends RBObject {
    public static Packet create(final Packet link, final int identity, final int kind) {
      Packet p = new Packet();
      p.initialize(link, identity, kind);
      return p;
    }

    private Packet link;
    private int    identity;
    private int    kind;
    private int    datum;
    private int[]  data;

    public int[] getData() { return data; }
    public int   getDatum() { return datum; }
    public void  setDatum(final int someData) { datum = someData; }

    public int  getIdentity() { return identity; }
    public void setIdentity(final int anIdentity) { identity = anIdentity; }

    public int getKind() { return kind; }
    public Packet getLink() { return link; }
    public void setLink(final Packet aLink) { link = aLink; }

    public void initialize(final Packet aLink, final int anIdentity, final int aKind) {
      link     = aLink;
      identity = anIdentity;
      kind     = aKind;
      datum    = 1;
      data     = new int[4];
    }
  }

  public static class TaskControlBlock extends TaskState {
    private TaskControlBlock link;
    private int identity;
    private int priority;
    private Packet input;
    private ProcessFunction function;
    private RBObject handle;

    public int getIdentity() { return identity; }
    public TaskControlBlock getLink()  { return link; }
    public int getPriority() { return priority; }

    public void initialize(final TaskControlBlock aLink, final int anIdentity,
        final int aPriority, final Packet anInitialWorkQueue,
        final TaskState anInitialState, final ProcessFunction aBlock,
        final RBObject aPrivateData) {
      link = aLink;
      identity = anIdentity;
      priority = aPriority;
      input = anInitialWorkQueue;
      setPacketPending(anInitialState.isPacketPending());
      setTaskWaiting(anInitialState.isTaskWaiting());
      setTaskHolding(anInitialState.isTaskHolding());
      function = aBlock;
      handle = aPrivateData;
    }

    TaskControlBlock addInputAndCheckPriority(final Packet packet,
        final TaskControlBlock oldTask) {
      if (RBObject.noWork() == input) {
        input = packet;
        setPacketPending(true);
        if (priority > oldTask.getPriority()) { return this; }
      } else {
        input = append(packet, input);
      }
      return oldTask;
    }

    TaskControlBlock runTask() {
      Packet message;
      if (isWaitingWithPacket()) {
        message = input;
        input = message.getLink();
        if (RBObject.noWork() == input) {
          running();
        } else {
          packetPending();
        }
      } else {
        message = RBObject.noWork();
      }
      return function.apply(message, handle);
    }

    public static TaskControlBlock create(final TaskControlBlock link, final int identity,
        final int priority, final Packet initialWorkQueue,
        final TaskState initialState, final ProcessFunction aBlock,
        final RBObject privateData) {
      TaskControlBlock t = new TaskControlBlock();
      t.initialize(link, identity, priority, initialWorkQueue,
          initialState, aBlock, privateData);
      return t;
    }
  }

  public static class TaskState extends RBObject {
    private boolean packetPending;
    private boolean taskWaiting;
    private boolean taskHolding;

    public boolean isPacketPending() { return packetPending; }
    public boolean isTaskHolding()   { return taskHolding;   }
    public boolean isTaskWaiting()   { return taskWaiting;   }

    public void setTaskHolding(final boolean b) { taskHolding = b; }
    public void setTaskWaiting(final boolean b) { taskWaiting = b; }
    public void setPacketPending(final boolean b) { packetPending = b; }

    public void packetPending() {
      packetPending = true;
      taskWaiting   = false;
      taskHolding   = false;
    }

    public void running() {
      packetPending = taskWaiting = taskHolding = false;
    }

    public void waiting() {
      packetPending = taskHolding = false;
      taskWaiting = true;
    }

    public void waitingWithPacket() {
      taskHolding = false;
      taskWaiting = packetPending = true;
    }

    public boolean isRunning() {
      return !packetPending && !taskWaiting && !taskHolding;
    }

    public boolean isTaskHoldingOrWaiting() {
      return taskHolding || (!packetPending && taskWaiting);
    }

    public boolean isWaiting() {
      return !packetPending && taskWaiting && !taskHolding;
    }

    public boolean isWaitingWithPacket() {
      return packetPending && taskWaiting && !taskHolding;
    }

    public static TaskState createPacketPending() {
      TaskState t = new TaskState();
      t.packetPending();
      return t;
    }

    public static TaskState createRunning() {
      TaskState t = new TaskState();
      t.running();
      return t;
    }

    public static TaskState createWaiting() {
      TaskState t = new TaskState();
      t.waiting();
      return t;
    }

    public static TaskState createWaitingWithPacket() {
      TaskState t = new TaskState();
      t.waitingWithPacket();
      return t;
    }
  }

  public static class WorkerTaskDataRecord extends RBObject {
    private int destination;
    private int count;

    public int getCount() { return count; }
    public void setCount(final int aCount) { count = aCount; }

    public int getDestination() { return destination; }
    public void setDestination(final int aHandler) { destination = aHandler; }

    private WorkerTaskDataRecord() {
      destination = RBObject.handlerA();
      count = 0;
    }

    public static WorkerTaskDataRecord create() {
      return new WorkerTaskDataRecord();
    }
  }

  public static class DeviceTaskDataRecord extends RBObject {
    private Packet pending;
    public Packet getPending() { return pending; }
    public void setPending(final Packet packet) { pending = packet; }

    private DeviceTaskDataRecord() {
      pending = RBObject.noWork();
    }

    public static DeviceTaskDataRecord create() {
      return new DeviceTaskDataRecord();
    }
  }

  public static class Transcript {
    public static void cr() { System.out.println(); }
    public static void show(final String text) { System.out.print(text); }
  }
}
