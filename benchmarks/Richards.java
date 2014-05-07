/*

 **  This is a Java version of the systems programming language benchmark.
 **  It uses very few object oriented features of the language.

 **  Implemented by M. Richards, based on the C version by M.J. Jordan.

 **  17/07/2007 Initial version
 **
 **  To run this benchmark, type:
 **
 **  make run
 **
 **  or
 **
 **  make run100
 **
 */

public class Richards {
	static int count = 0;

	static int Count = 10000;
	static int Qpktcountval = 23246;
	static int Holdcountval = 9297;

	static int Count100 = 10000 * 100;
	static int Qpktcountval100 = 2326410;
	static int Holdcountval100 = 930563;

	final static int MAXINT = 32767;

	final static int BUFSIZE = 3;
	final static int I_IDLE = 1;
	final static int I_WORK = 2;
	final static int I_HANDLERA = 3;
	final static int I_HANDLERB = 4;
	final static int I_DEVA = 5;
	final static int I_DEVB = 6;
	final static int PKTBIT = 1;
	final static int WAITBIT = 2;
	final static int HOLDBIT = 4;
	final static int NOTPKTBIT = ~1;
	final static int NOTWAITBIT = ~2;
	final static int NOTHOLDBIT = 0xFFFB;

	final static int S_RUN = 0;
	final static int S_RUNPKT = 1;
	final static int S_WAIT = 2;
	final static int S_WAITPKT = 3;
	final static int S_HOLD = 4;
	final static int S_HOLDPKT = 5;
	final static int S_HOLDWAIT = 6;
	final static int S_HOLDWAITPKT = 7;

	final static int K_DEV = 1000;
	final static int K_WORK = 1001;

	static String alphabet = "#ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	static Tasktab tasktab = new Tasktab(11);

	static Task tasklist = null;
	static Task tcb;
	static int taskid;
	static Object v1; // These are meant to be like CPU registers, ie
	static Object v2; // saved as part of the task state.
	static int qpktcount = 0;
	static int holdcount = 0;
	static boolean tracing = false;
	static int layout = 0;

	public static void main(final String[] args) {
		// args
		// num-iterations
		// warm-up
		// inner-iter

		tracing = false;

		int numIterations = Integer.valueOf(args[0]);
		int warmUp        = Integer.valueOf(args[1]);
		int innerIter     = Integer.valueOf(args[2]);

		int result = 0;

		for (int i = warmUp; i > 0; i--) {
			result += innerLoop(innerIter);
		}

		for (int i = numIterations; i > 0; i--) {


			long start = System.nanoTime();
			result += innerLoop(innerIter);
			long end = System.nanoTime();
			long microseconds = (end - start) / 1000;

			System.out.println("Richards: iterations=1 runtime: " + microseconds + "us");
		}

		System.out.println(result);
	}

	private static int innerLoop(int inner) {
		int r = 0;
		int i = 0;
		while (inner > 0) {
			r += benchmark();
			inner--;
		}
		return r;
	}

	private static int benchmark() {
    // reset everything
    tasktab = new Tasktab(11);
    tasklist = null;
    qpktcount = holdcount = 0;

		Packet wkq = null;

		// System.out.println("Bench mark starting");

		new IdleTask(I_IDLE, 0, wkq, S_RUN, 1, Count);

		wkq = new Packet(null, 0, K_WORK);
		wkq = new Packet(wkq, 0, K_WORK);

		new WorkTask(I_WORK, 1000, wkq, S_WAITPKT, I_HANDLERA, 0);

		wkq = new Packet(null, I_DEVA, K_DEV);
		wkq = new Packet(wkq, I_DEVA, K_DEV);
		wkq = new Packet(wkq, I_DEVA, K_DEV);

		new HandlerTask(I_HANDLERA, 2000, wkq, S_WAITPKT, null, null);

		wkq = new Packet(null, I_DEVB, K_DEV);
		wkq = new Packet(wkq, I_DEVB, K_DEV);
		wkq = new Packet(wkq, I_DEVB, K_DEV);

		new HandlerTask(I_HANDLERB, 3000, wkq, S_WAITPKT, null, null);

		wkq = null;
		new DevTask(I_DEVA, 4000, wkq, S_WAIT, 0, 0);
		new DevTask(I_DEVB, 5000, wkq, S_WAIT, 0, 0);

		tcb = tasklist;

//		System.out.println("Starting");

		layout = 0;

		schedule();

//		System.out.println("\nfinished\n");

//		System.out.println("qpkt count = " + qpktcount + "  holdcount = "
//				+ holdcount);

		// System.out.print("These results are ");
		if (qpktcount == Qpktcountval && holdcount == Holdcountval) {
			// System.out.println("correct");
		} else {
			throw new RuntimeException("The results are incorrect!");
		}

		// System.out.println("end of run");
		return qpktcount + holdcount;
	}

	static void schedule() {
		while (tcb != null) {
			Packet pkt = null;
			Task newtcb;

			// System.out.println("Task "+tcb.id+" state="+tcb.state);

			switch (tcb.state) {
			case S_WAITPKT:
				pkt = tcb.wkq;
				tcb.wkq = pkt.link;
				tcb.state = (tcb.wkq == null) ? S_RUN : S_RUNPKT;

			case S_RUN:
			case S_RUNPKT:
				taskid = tcb.id;
				v1 = tcb.sv1;
				v2 = tcb.sv2;
				if (tracing) {
					// trace('T');
					trace(taskid + '0');
					// trace(' ');
				}
				newtcb = tcb.fn(pkt);
				tcb.sv1 = v1;
				tcb.sv2 = v2;
				tcb = newtcb;
				// System.out.println("schedule: newtcb.id="+tcb.id);
				break;

			case S_WAIT:
			case S_HOLD:
			case S_HOLDPKT:
			case S_HOLDWAIT:
			case S_HOLDWAITPKT:
				tcb = tcb.link;
				break;

			default:
				return;
			}
		}
	}

	public static void trace(final int a) {
		if (--layout <= 0) {
			System.out.println("");
			layout = 50;
		}

		System.out.print((char) a);
	}

	public static Task release(final int id) {
		Task t;

		t = findtcb(id);
		if (t == null) {
			return null;
		}

		t.state &= NOTHOLDBIT;
		if (t.pri > tcb.pri) {
			return t;
		}

		return tcb;
	}

	public static Task taskwait() {
		tcb.state |= WAITBIT;
		return tcb;
	}

	public static Task holdself() {
		++holdcount;
		tcb.state |= HOLDBIT;
		return tcb.link;
	}

	static Task findtcb(final int id) {
		Task t = null;

		if (1 <= id && id <= tasktab.upb) {
			t = tasktab.v[id];
		}
		if (t == null) {
			System.out.println("\nBad task id " + id);
		}
		return t;
	}

	public static Task qpkt(final Packet pkt) {
		Task t = findtcb(pkt.id);

		if (t == null) {
			return null;
		}

		qpktcount++;

		// System.out.println("qpkt: sending pkt from T"+taskid+" to T"+pkt.id);
		pkt.link = null;
		pkt.id = taskid;

		if (t.wkq == null) {
			t.wkq = pkt;
			t.state |= PKTBIT;
			// System.out.println("qpkt: dest pri = "+t.pri+" curr pri = "+tcb.pri);
			if (t.pri > tcb.pri) {
				return t;
			}
		} else {
			t.wkq = append(pkt, t.wkq);
		}

		return tcb;
	}

	public static Packet append(final Packet pkt, Packet p) {
		pkt.link = null;

		if (p == null) {
			return pkt;
		}

		Packet q = p;
		while (p.link != null) {
			p = p.link;
		}

		p.link = pkt;

		return q;
	}
}

class Packet {
	Packet link;
	int id;
	int kind;
	int a1;
	int[] a2;

	public Packet(final Packet link, final int id, final int kind) {
		this.link = link;
		this.id = id;
		this.kind = kind;
		a2 = new int[Richards.BUFSIZE + 1];
		for (int i = 0; i < Richards.BUFSIZE; i++) {
			a2[i] = 0;
		}
	}
};

abstract class Task {
	Task link;
	int id;
	int pri;
	Packet wkq;
	int state;
	Object sv1; // Saved values of v1 and v2 which are meant to be like
	Object sv2; // general purpose registers, ie part of the task state.

	Task(final int id, final int pri, final Packet wkq, final int state,
			final Object v1, final Object v2) {
		this.link = Richards.tasklist;
		this.id = id;
		this.pri = pri;
		this.wkq = wkq;
		this.state = state;
		sv1 = v1;
		sv2 = v2;
		Richards.tasklist = this;
		Richards.tasktab.v[id] = this;
	}

	abstract Task fn(Packet pkt);
};

class IdleTask extends Task {

	IdleTask(final int id, final int pri, final Packet wkq, final int state,
			final Object v1, final Object v2) {
		super(id, pri, wkq, state, v1, v2);
	}

	@Override
	Task fn(final Packet pkt) {
		int x = ((Integer) Richards.v2).intValue() - 1;
		Richards.v2 = new Integer(x);

		if (x == 0) {
			return (Richards.holdself());
		}

		x = ((Integer) Richards.v1).intValue();

		if ((x & 1) == 0) {
			Richards.v1 = new Integer((x >> 1) & Richards.MAXINT);
			return (Richards.release(Richards.I_DEVA));
		} else {
			Richards.v1 = new Integer(((x >> 1) & Richards.MAXINT) ^ 0XD008);
			return (Richards.release(Richards.I_DEVB));
		}
	}
};

class WorkTask extends Task {

	WorkTask(final int id, final int pri, final Packet wkq, final int state,
			final Object v1, final Object v2) {
		super(id, pri, wkq, state, v1, v2);
	}

	@Override
	Task fn(final Packet pkt) {
		if (pkt == null) {
			return Richards.taskwait();
		} else {
			int x = Richards.I_HANDLERA + Richards.I_HANDLERB
					- ((Integer) Richards.v1).intValue();
			Richards.v1 = new Integer(x);

			pkt.id = x;
			// System.out.println("work: pkt.id="+x);
			pkt.a1 = 0;
			for (int i = 0; i <= Richards.BUFSIZE; i++) {
				x = ((Integer) Richards.v2).intValue() + 1;

				if (x > 26) {
					x = 1;
				}
				Richards.v2 = new Integer(x);
				(pkt.a2)[i] = Richards.alphabet.charAt(x);
				// System.out.println("work fn: v2 = "+x+
				// " ch="+(char)(pkt.a2)[i]);
			}
			return Richards.qpkt(pkt);
		}
	}
};

class HandlerTask extends Task {

	HandlerTask(final int id, final int pri, final Packet wkq, final int state,
			final Object v1, final Object v2) {
		super(id, pri, wkq, state, v1, v2);
	}

	@Override
	Task fn(final Packet pkt) {
		if (pkt != null) {
			if (pkt.kind == Richards.K_WORK) {
				Richards.v1 = Richards.append(pkt, (Packet) Richards.v1);
			} else {
				Richards.v2 = Richards.append(pkt, (Packet) Richards.v2);
			}
		}

		if (Richards.v1 != null) {
			Packet workpkt = ((Packet) Richards.v1);
			int count = workpkt.a1;
			// System.out.println("Handler: count="+count);
			if (count > Richards.BUFSIZE) {
				Richards.v1 = workpkt.link;
				return Richards.qpkt(workpkt);
			}

			if (Richards.v2 != null) {
				Packet devpkt = (Packet) Richards.v2;
				Richards.v2 = devpkt.link;
				devpkt.a1 = workpkt.a2[count];
				// System.out.println("Handler: setting a1="+devpkt.a1);
				workpkt.a1 = count + 1;
				return Richards.qpkt(devpkt);
			}

		}
		return Richards.taskwait();
	}
};

class DevTask extends Task {

	DevTask(final int id, final int pri, final Packet wkq, final int state,
			final int v1, final int v2) {
		super(id, pri, wkq, state, v1, v2);
	}

	@Override
	Task fn(Packet pkt) {
		if (pkt == null) {
			if (Richards.v1 == null) {
				return (Richards.taskwait());
			}
			pkt = (Packet) Richards.v1;
			Richards.v1 = null;
			return Richards.qpkt(pkt);
		} else {
			Richards.v1 = pkt;
			if (Richards.tracing) {
				// Bench.trace('K');
				Richards.trace(pkt.a1);
				// Bench.trace(' ');
			}
			return Richards.holdself();
		}
	}
};

class Tasktab {
	int upb;
	Task v[];

	public Tasktab(final int upb) {
		this.upb = upb;
		this.v = new Task[upb + 1];
	}
}
