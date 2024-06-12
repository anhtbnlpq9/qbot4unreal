package xyz.mjav.theqbot;

import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;

import xyz.mjav.theqbot.exceptions.BeiNotFoundException;

/**
 * BeiList should be consistent with Map<Bei, Map<String, Object>>
 *
 * Bei: [
 *   author:
 *   fromTS:
 *   toTS:
 *   reason:
 * ]
 *
 */
public class BeiList {

    private Map<Bei, BeiProperty> list = new HashMap<>();

    public BeiList() {

    }

    public void put(Bei bei, BeiProperty beiProp) {
        this.list.put(bei, beiProp);
    }

    public void remove(Bei bei) {
        this.list.remove(bei);
    }

    public Set<Bei> keySet() {
        return list.keySet();
    }

    public Set<Map.Entry<Bei, BeiProperty>> entrySet() {
        return list.entrySet();
    }

    public int size() {
        return list.size();
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    //public void forEach(BiConsumer<? super Bei, ? super BeiProperty> action) {
    //    return list.forEach(action);
    //}


    public void forEach(BiConsumer<? super Bei, ? super BeiProperty> action) {
      Objects.requireNonNull(action);

      Bei k;
      BeiProperty v;
      for(Iterator<Map.Entry<Bei, BeiProperty>> var2 = this.entrySet().iterator(); var2.hasNext(); action.accept(k, v)) {
         Entry<Bei, BeiProperty> entry = (Entry<Bei, BeiProperty>)var2.next();

         try {
            k = entry.getKey();
            v = entry.getValue();
         } catch (IllegalStateException var7) {
            throw new ConcurrentModificationException(var7);
         }
      }

   }



    public BeiProperty get(Bei bei) throws BeiNotFoundException {
        if (list.containsKey(bei)) return list.get(bei);
        else throw new BeiNotFoundException();
    }

}
