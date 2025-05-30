package org.testng.internal;

import org.testng.IAttributes;
import org.testng.IAlterTestName;
import org.testng.IClass;
import org.testng.ITest;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.TestNGException;
import org.testng.collections.Objects;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

/**
 * This class represents the result of a test.
 */
public class TestResult implements ITestResult, IAlterTestName {

  private IClass m_testClass = null;
  private ITestNGMethod m_method = null;
  private int m_status = CREATED;
  private Throwable m_throwable = null;
  private long m_startMillis = 0;
  private long m_endMillis = 0;
  private String m_name = null;
  private String m_host;
  private Object[] m_parameters = {};
  private Object m_instance;
  private String m_instanceName;
  private ITestContext m_context;
  private int parameterIndex;

  public TestResult() {

  }

  public TestResult(Object instance, ITestNGMethod method, Throwable throwable, ITestContext context) {
    long time = System.currentTimeMillis();
    init(method.getTestClass(), instance, method, throwable, time, time, context);
  }

  public TestResult(IClass testClass,
      Object instance,
      ITestNGMethod method,
      Throwable throwable,
      long start,
      long end,
      ITestContext context)
  {
    init(testClass, instance, method, throwable, start, end, context);
  }

  public void init (IClass testClass,
      Object instance,
      ITestNGMethod method,
      Throwable throwable,
      long start,
      long end,
      ITestContext context)
  {
    m_testClass = testClass;
    m_throwable = throwable;
    m_instanceName = m_testClass.getName();
    if (null == m_throwable) {
      m_status = ITestResult.SUCCESS;
    }
    m_startMillis = start;
    m_endMillis = end;
    m_method = method;
    m_context = context;

    m_instance = instance;

    // Calculate the name: either the method name, ITest#getTestName or
    // toString() if it's been overridden.
    if (m_instance == null) {
      m_name = m_method.getMethodName();
    } else {
      if (m_instance instanceof ITest) {
        m_name = ((ITest) m_instance).getTestName();
      } else if (testClass.getTestName() != null) {
        m_name = testClass.getTestName();
      } else {
        String string = m_instance.toString();
        // Only display toString() if it's been overridden by the user
        m_name = getMethod().getMethodName();
        try {
          if (!Object.class.getMethod("toString")
              .equals(m_instance.getClass().getMethod("toString"))) {
            m_instanceName = string.startsWith("class ")
                ? string.substring("class ".length())
                : string;
            m_name = m_name + " on " + m_instanceName;
          }
        } catch(NoSuchMethodException ignore) {
          // ignore
        }
      }
    }
  }

  @Override
  public void setEndMillis(long millis) {
    m_endMillis = millis;
  }

  /**
   * If this result's related instance implements ITest or use @Test(testName=...), returns its test name,
   * otherwise returns null.
   */
  @Override
  public String getTestName() {
    if (m_instance instanceof ITest) {
      return ((ITest) m_instance).getTestName();
    }
    if (m_testClass.getTestName() != null) {
      return m_testClass.getTestName();
    }
    return null;
  }

  @Override
  public String getName() {
    return m_name;
  }

  /**
   * @return Returns the method.
   */
  @Override
  public ITestNGMethod getMethod() {
    return m_method;
  }

  /**
   * @param method The method to set.
   */
  public void setMethod(ITestNGMethod method) {
    m_method = method;
  }

  /**
   * @return Returns the status.
   */
  @Override
  public int getStatus() {
    return m_status;
  }

  /**
   * @param status The status to set.
   */
  @Override
  public void setStatus(int status) {
    m_status = status;
  }

  @Override
  public boolean isSuccess() {
    return ITestResult.SUCCESS == m_status;
  }

  /**
   * @return Returns the testClass.
   */
  @Override
  public IClass getTestClass() {
    return m_testClass;
  }

  /**
   * @param testClass The testClass to set.
   */
  public void setTestClass(IClass testClass) {
    m_testClass = testClass;
  }

  /**
   * @return Returns the throwable.
   */
  @Override
  public Throwable getThrowable() {
    return m_throwable;
  }

  /**
   * @param throwable The throwable to set.
   */
  @Override
  public void setThrowable(Throwable throwable) {
    m_throwable = throwable;
  }

  /**
   * @return Returns the endMillis.
   */
  @Override
  public long getEndMillis() {
    return m_endMillis;
  }

  /**
   * @return Returns the startMillis.
   */
  @Override
  public long getStartMillis() {
    return m_startMillis;
  }

  @Override
  public String toString() {
    List<String> output = Reporter.getOutput(this);
    return Objects.toStringHelper(getClass())
        .omitNulls()
        .omitEmptyStrings()
        .add("name", getName())
        .add("status", toString(m_status))
        .add("method", m_method)
        .add("output", output != null && output.size() > 0 ? output.get(0) : null)
        .toString();

  }

  private static String toString(int status) {
    switch(status) {
      case SUCCESS: return "SUCCESS";
      case FAILURE: return "FAILURE";
      case SKIP: return "SKIP";
      case SUCCESS_PERCENTAGE_FAILURE: return "SUCCESS WITHIN PERCENTAGE";
      case STARTED: return "STARTED";
      case CREATED: return "CREATED";
      default: throw new TestNGException("Encountered an un-defined test status of [" + status + "].");
    }
  }

  @Override
  public String getHost() {
    return m_host;
  }

  public void setHost(String host) {
    m_host = host;
  }

  @Override
  public Object[] getParameters() {
    return m_parameters;
  }

  @Override
  public void setParameters(Object[] parameters) {
    m_parameters = new Object[parameters.length];
    for (int i = 0; i < parameters.length; i++) {
      // Copy parameter if possible because user may change it later
      if (parameters[i] instanceof Cloneable) {
        try {
          Method clone = parameters[i].getClass().getDeclaredMethod("clone");
          m_parameters[i] = clone.invoke(parameters[i]);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | SecurityException e) {
          m_parameters[i] = parameters[i];
        }
      } else {
        m_parameters[i] = parameters[i];
      }
    }
  }

  @Override
  public Object getInstance() {
    return m_instance;
  }

  private final IAttributes m_attributes = new Attributes();

  @Override
  public Object getAttribute(String name) {
    return m_attributes.getAttribute(name);
  }

  @Override
  public void setAttribute(String name, Object value) {
    m_attributes.setAttribute(name, value);
  }

  @Override
  public Set<String> getAttributeNames() {
    return m_attributes.getAttributeNames();
  }

  @Override
  public Object removeAttribute(String name) {
    return m_attributes.removeAttribute(name);
  }

  @Override
  public ITestContext getTestContext() {
	  return m_context;
  }

  public void setContext(ITestContext context) {
	  m_context = context;
  }

  @Override
  public int compareTo(ITestResult comparison) {
    return Long.compare(getStartMillis(), comparison.getStartMillis());
  }

  @Override
  public String getInstanceName() {
    return m_instanceName;
  }

  @Override
  public void setTestName(String name) {
    m_name = name;
  }

  void setParameterIndex(int parameterIndex) {
    this.parameterIndex = parameterIndex;
  }

  public int getParameterIndex() {
    return parameterIndex;
  }
}

