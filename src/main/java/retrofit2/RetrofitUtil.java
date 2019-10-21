package retrofit2;

import javax.annotation.Nullable;
import java.lang.reflect.Method;

public class RetrofitUtil {
    static private final Object[] emptyArgs = new Object[0];
    @Nullable
    static public <T> Call<T> invokeServiceMethod(Retrofit r, Method m, Object[] args) {
        return (Call<T>)r.loadServiceMethod(m).invoke(args != null ? args : emptyArgs);
    }
}
