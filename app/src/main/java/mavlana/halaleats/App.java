package mavlana.halaleats;

/**
 * Created by samir on 19/09/15.
 */
import android.app.Application;
import com.parse.Parse;
import com.parse.ParseCrashReporting;

public class App extends Application {

    @Override public void onCreate() {
        super.onCreate();

        Parse.enableLocalDatastore(this);
        ParseCrashReporting.enable(this);

        Parse.initialize(this, "6ZEjSMecAtcZpqjfYcCckpJ6Eey77blAifHaJxYN", "SMo3TDBVtsO5BugX1xkYJp6iiIgT4ixq8B5FJATD");
    }
}



