package com.hbhs.algorithm.leecode;

import java.util.BitSet;

/**
 * Your RandomizedSet object will be instantiated and called as such:
 * RandomizedSet obj = new RandomizedSet();
 * boolean param_1 = obj.insert(val);
 * boolean param_2 = obj.remove(val);
 * int param_3 = obj.getRandom();
 */
public class RandomizedSet {
    BitSet bit = new BitSet();
    public static void main(String[] args){
        RandomizedSet data = new RandomizedSet();
        int i=1;
        while(i<20){
            data.insert(i);
            i++;
        }



    }

    /** Initialize your data structure here. */
    public RandomizedSet() {

    }

    /** Inserts a value to the set. Returns true if the set did not already contain the specified element. */
    public boolean insert(int val) {
        if (bit.get(val)) return false;
        bit.set(val);
        return true;
    }

    /** Removes a value from the set. Returns true if the set contained the specified element. */
    public boolean remove(int val) {
        if (!bit.get(val)) return false;
        bit.set(val, false);
        return true;
    }

    /** Get a random element from the set. */
    public int getRandom() {
        int index = (int)(bit.size()*Math.random());
        int returnIndex = bit.nextSetBit(index);
        if (returnIndex==-1)
            returnIndex = bit.nextSetBit(0);
        return returnIndex;
    }
}