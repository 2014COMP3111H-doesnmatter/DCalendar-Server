package doesnutil;

import java.util.Collection;
import java.util.Iterator;

public class WrapperUtil
{

	public static long[] toArray(Collection<Long> c) {
		long[] rtn = new long[c.size()];
		Iterator<Long> itr = c.iterator();
		for(int i=0;itr.hasNext();i++) {
			rtn[i] = itr.next();
		}
		return rtn;
	}
	public static void toCollection(long[] a, Collection<Long> c) {
		c.clear();
		for(long l:a) {
			c.add(l);
		}
	}

}
