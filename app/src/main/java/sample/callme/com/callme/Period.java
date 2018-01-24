package sample.callme.com.callme;

import android.util.Log;


public enum Period {

    DAY, YESTERDAY, WEEK, MONTH;

    public int asInt() {
        switch(this) {
            case DAY: return 1;
            case YESTERDAY: return 2;
            case WEEK: return 3;
            case MONTH: return 4;
            default:
                throw new RuntimeException("Unsupported period: " + this);
        }
    }

    public static Period fromInt(int x) {
        switch(x) {
            case 1: return DAY;
            case 2: return YESTERDAY;
            case 3: return WEEK;
            case 4: return MONTH;
            default:
                throw new RuntimeException("Unsupported period: " + x);
        }
    }
}
