package eu.fox7.rexp.isc.fast.run;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import eu.fox7.rexp.util.FileX;
import eu.fox7.rexp.util.Log;
import org.objenesis.strategy.StdInstantiatorStrategy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class EvalTreeSerialiser {
	static Kryo getKryo() {
		Kryo _kryo = new Kryo();
		_kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
		return _kryo;
	}

	private static final Kryo kryo = getKryo();

	public static void saveObjectToFile(Object object, String fileName) {
		try {
			File file = FileX.newFile(fileName);
			FileOutputStream fos = new FileOutputStream(file);
			Output output = new Output(fos);
			kryo.writeObject(output, object);
			output.flush();
			output.close();
		} catch (FileNotFoundException ex) {
			Log.w("Could not serialize object: %s", ex);
		}
	}

	public static Object loadFromFile(String fileName, Class<?> type) {
		try {
			File file = FileX.newFile(fileName);
			FileInputStream fis = new FileInputStream(file);
			Input input = new Input(fis);
			Object result = kryo.readObject(input, type);
			input.close();
			return result;
		} catch (FileNotFoundException ex) {
			Log.w("Could not serialize object: %s", ex);
			return null;
		}
	}

	public static boolean safeSaveObjectToFile(Object object, String fileName) {
		try {
			saveObjectToFile(object, fileName);
			return true;
		} catch (StackOverflowError ex) {
			return false;
		}
	}
}
