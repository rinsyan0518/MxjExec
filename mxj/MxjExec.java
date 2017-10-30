import com.cycling74.max.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

public class MxjExec extends MaxObject {
    private boolean mIsRunning;
    private Thread mRunThread;
    private Process mProcess;

    public MxjExec() {
        mIsRunning = false;
        declareIO(1, 0);
    }
    public void exec(Atom[] args) {
        if (mIsRunning) {
            post("Running");
            return;
        }

        String[] cmds = new String[args.length];
        for (int i = 0; i < args.length; i++) {
            cmds[i] = args[i].getString();
        }

        try {
            mIsRunning = true;
            ProcessBuilder pb = new ProcessBuilder(cmds);
            pb.redirectErrorStream(true);

            mProcess = pb.start();
            post("Execute Process");
            mRunThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    String str;
                    BufferedReader br = new BufferedReader(new InputStreamReader(mProcess.getInputStream()));
                    try {
                        while((str = br.readLine()) != null) {
                            post(str);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            br.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        mIsRunning = false;
                    }
                }
            });
            mRunThread.start();
        } catch (Exception e) {
            mIsRunning = false;
            e.printStackTrace();
        }
    }

    public void stop() {
        try {
            if (mProcess != null)
                mProcess.destroy();
            mProcess = null;
            mIsRunning = false;
            post("Stop Process");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void notifyDeleted() {
        stop();
    }
}

