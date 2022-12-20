import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Node {
	private List<String> sequence ;
	private Map<Integer, List<Element>> u_chain ;

	public Node() {
		this.sequence  = new ArrayList<>();
		this.u_chain = new HashMap<>();
	}

	public List<String> getSequence() {
		return sequence;
	}

	public void addItemSet(String itemset) {
		sequence.add(itemset);
	}

	public Map<Integer, List<Element>> getu_chain() {
		return u_chain;
	}
	public void addElement(Integer sid, Element element) {
		List<Element> list = this.u_chain.get(sid);
		if (list == null) {
			List<Element> elements = new ArrayList<Element>();
			elements.add(element);
			u_chain.put(sid, elements);
		} else {
			list.add(element);
		}
	}
	public Integer getUtility() {
		Integer utility = 0;
		for (Integer key : u_chain.keySet()) {
			Integer max = Integer.MIN_VALUE;
			List<Element> valueList = u_chain.get(key);
			for (Element value : valueList) {

				max=Math.max(max,value.getAcu());

			}
			utility += max;
		}
		return utility;
	}

	public Integer getPEU() {
		Integer peu = 0;
		Integer lastItem=getLastItem(sequence);
		for (Integer key : u_chain.keySet()) {
			List<Element> valueList = u_chain.get(key);
			Integer max = Integer.MIN_VALUE;
			List<Map<Integer,PairVal>> matrix=HUSPanN.matrixSet.get(key-1);
			for (int i = 0; i < valueList.size(); i++) {
				int tmp=0;
				//?????????????????
				if (i==valueList.size()-1 && lastItem.equals(getLastItem2(matrix))){
					tmp=0;
				}else {
					tmp = valueList.get(i).getAcu() + valueList.get(i).getRu();
				}

				max=Math.max(max,tmp);
			}
			/*for (Element value : valueList) {

				Integer tmp = 0;
				if (value.getRu() > 0) {
					tmp = value.getAcu() + value.getRu();
				}
				max=Math.max(max,tmp);
			}*/
			peu += max;
		}
		return peu;
	}
	/**
	 * Calculate the PEU value of each sid (sequence)
	 *
	 * @param sid
	 * @return
	 */
	public Integer getSequencePEU(Integer sid) {
		List<Element> elementList = u_chain.get(sid);
		Integer max = Integer.MIN_VALUE;
		List<Map<Integer,PairVal>> matrix=HUSPanN.matrixSet.get(sid-1);
		Integer lastItem=getLastItem(sequence);
		// for each element
		if (elementList != null || elementList.size() > 0) {
			// for each item in this element
			for (int i = 0; i < elementList.size(); i++) {
				int tmp=0;
				//?????????????????
				if (i==elementList.size()-1 && lastItem.equals(getLastItem2(matrix))){
					tmp=0;
				}else {
					tmp = elementList.get(i).getAcu() + elementList.get(i).getRu();
				}

				max=Math.max(max,tmp);
			}
			return max;
		}
		return 0;
	}
	private Integer getLastItem2(List<Map<Integer,PairVal>> matrix){
		Map<Integer,PairVal> last=matrix.get(matrix.size()-1);
		List<Integer> keyset=last.keySet().stream().collect(Collectors.toList());
		/*for (int i = 0; i < last.size(); i++) {
			if (i==last.size()-1){
				return last.get()
			}
		}*/
		return keyset.get(keyset.size()-1);
	}
	private Integer getLastItem(List<String> sequence) {
		String lastItemset = sequence.get(sequence.size() - 1);
		Integer lastItem = -1;
		if (!lastItemset.contains("-")) {
			lastItem = Integer.parseInt(lastItemset);
		} else {
			String[] splits = lastItemset.split("-");
			lastItem = Integer.parseInt(splits[splits.length - 1]);
		}

		return lastItem;
	}

	public void addElementForSExtendsion(Integer sid, Element element) {
		List<Element> list = u_chain.get(sid);
		if (list == null || list.size() == 0) {
			// the first item
			List<Element> elements = new ArrayList<>();
			elements.add(element);
			u_chain.put(sid, elements);;
		} else {
			int index = -1;
			for (int i = 0; i < list.size(); i++) {
				Element e = list.get(i);
				if (e.getTid().intValue() == element.getTid().intValue()) {
					index = i;
					break;
				}
			}
			if (index != -1) {
				Element e = list.get(index);

				// update the ACU value
				if (e.getAcu() < element.getAcu()) {
					e.setAcu(element.getAcu());
				}
			} else {
				//Element ele = list.get(list.size() - 1);
				//ele.setNext(list.size());
				list.add(element);
				u_chain.put(sid, list);
			}
		}
	}

	public void printNode() {
		String str = "";
		for (String s : sequence) {
			str += s + ";";
		}
		str = str.substring(0, str.length() - 1);
		System.out.println("---------------" + str + "-----------------");
		//Map<Integer, List<Element>> uttilityChain = u_chain.getUttilityChain();
		for (Map.Entry<Integer, List<Element>> me : u_chain.entrySet()) {
			List<Element> value = me.getValue();
			for (Element e : value) {
				System.out.print( + e.getTid() + " " + e.getAcu() + " " + e.getRu() + " " );
			}
			System.out.println();
		}
		System.out.println("---------------" + str + "-----------------");
	}
}
