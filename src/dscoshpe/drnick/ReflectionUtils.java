package dscoshpe.drnick;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;

// adapted from: https://github.com/Tombarr/Signal-Strength-Detector
public final class ReflectionUtils
{
	public static final String TAG = ReflectionUtils.class.getSimpleName();
	
	/**
	 * Dumps a {@link Class}'s {@link Method}s and {@link Field}s
	 * as a String.
	 */
	public static final void dumpDetails(HashMap<String, String> details, Class<?> mClass, Object mInstance, String prefix)
	{
		if (mClass == null || mInstance == null) return;
		
		//HashMap<String, Object> details = new HashMap<String, Object>();
		
		final Field[] mFields = mClass.getDeclaredFields();
		
		for (final Field mField : mFields)
		{
			mField.setAccessible(true);
			
			String key = mField.getName();
			String value = "";
			
			try
			{
				value = mField.get(mInstance).toString();
			}
			catch (Exception e)
			{
				value = "null";
				//Log.e(TAG, "Could not get Field `" + mField.getName() + "`.", e);
			}
			
			if (key.startsWith("m") && !(value.contentEquals("null") || value.contentEquals("[]")))
				details.put(String.format("%s%s", prefix, key), value);
		}
		
		// Dump all methods.
		
		final Method[] mMethods = mClass.getMethods();
		
		for (final Method mMethod : mMethods)
		{
			mMethod.setAccessible(true);
		
			String key = mMethod.getName();
			String value = "";
			
			try
			{
				final Object mRet = mMethod.invoke(mInstance);
				value = (mRet == null) ? "null" : mMethod.invoke(mInstance).toString();
			}
			catch (Exception e)
			{
				value = "null";
				//Log.e(TAG, "Could not get Method `" + mMethod.getName() + "`.", e);
			}
			
			if ((key.startsWith("get") || key.startsWith("is")) && 
					!(value.contentEquals("null") || value.contentEquals("[]")))
				details.put(String.format("%s%s", prefix, key), value);
		}
	}
	
	
	/**
	 * Dumps a {@link Class}'s {@link Method}s and {@link Field}s
	 * as a String.
	 */
	public static final String dumpClass(Class<?> mClass, Object mInstance)
	{
		if (mClass == null || mInstance == null) return null;
		
		String mStr = mClass.getSimpleName() + "\n\n";
		
		mStr += "FIELDS\n\n";
		
		final Field[] mFields = mClass.getDeclaredFields();
		
		for (final Field mField : mFields)
		{
			mField.setAccessible(true);
		
			mStr += mField.getName() + " (" + mField.getType() + ") = ";
			
			try
			{
				mStr += mField.get(mInstance).toString();
			}
			catch (Exception e)
			{
				mStr += "null";
				//Log.e(TAG, "Could not get Field `" + mField.getName() + "`.", e);
			}
			
			mStr += "\n";
		}
		
		mStr += "METHODS\\nn";
		
		// Dump all methods.
		
		final Method[] mMethods = mClass.getMethods();
		
		for (final Method mMethod : mMethods)
		{
			mMethod.setAccessible(true);
		
			mStr += mMethod.getReturnType() + " " + mMethod.getName() + "() = ";
			
			try
			{
				final Object mRet = mMethod.invoke(mInstance);
				mStr += (mRet == null) ? "null" : mMethod.invoke(mInstance).toString();
			}
			catch (Exception e)
			{
				mStr += "null";
				//Log.e(TAG, "Could not get Method `" + mMethod.getName() + "`.", e);
			}
			
			mStr += "\n";
		}
		
		return mStr;
	}
	
	/**
	 * @return A string containing the values of all static {@link Field}s.
	 */
	public static final String dumpStaticFields(Class<?> mClass, Object mInstance)
	{
		if (mClass == null || mInstance == null) return null;
		
		String mStr = mClass.getSimpleName() + "\n\n";
		
		mStr += "STATIC FIELDS\n\n";
		
		final Field[] mFields = mClass.getDeclaredFields();
		
		for (final Field mField : mFields)
		{
			if (ReflectionUtils.isStatic(mField))
			{
				mField.setAccessible(true);
			
				mStr += mField.getName() + " (" + mField.getType() + ") = ";
				
				try
				{
					mStr += mField.get(mInstance).toString();
				}
				catch (Exception e)
				{
					mStr += "null";
					//Log.e(TAG, "Could not get Field `" + mField.getName() + "`.", e);
				}
				
				mStr += "\n";
			}
		}
		
		return mStr;
	}
	
	/**
	 * @return True if the {@link Field} is static.
	 */
	public final static boolean isStatic(Field field)
	{
    	final int modifiers = field.getModifiers();
    	return (Modifier.isStatic(modifiers));
    }
}
