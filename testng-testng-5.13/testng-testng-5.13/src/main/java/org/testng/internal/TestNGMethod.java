package org.testng.internal;

import java.lang.reflect.Method;
import java.util.Comparator;

import org.testng.ITestClass;
import org.testng.ITestNGMethod;
import org.testng.annotations.ITestAnnotation;
import org.testng.internal.annotations.AnnotationHelper;
import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.xml.XmlTest;


/**
 * This class represents a test method.
 *
 * @author Cedric Beust, May 3, 2004
 * @author <a href = "mailto:the_mindstorm&#64;evolva.ro">Alexandru Popescu</a>
 */
public class TestNGMethod extends BaseTestMethod {
  private int m_threadPoolSize = 0;
  private int m_invocationCount = 1;
  private int m_successPercentage = 100;
  private long m_timeOut = 0;
  private XmlTest m_xmlTest;

  /**
   * Constructs a <code>TestNGMethod</code>
   *
   * @param method
   * @param finder
   */
  public TestNGMethod(Method method, IAnnotationFinder finder, XmlTest xmlTest) {
    this(method, finder, true, xmlTest);
  }

  private TestNGMethod(Method method, IAnnotationFinder finder, boolean initialize, XmlTest xmlTest) {
    super(method, finder);

    if(initialize) {
      init(xmlTest);
    }
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public long getTimeOut() {
    long result = m_timeOut != 0 ? m_timeOut : (m_xmlTest != null ? m_xmlTest.getTimeOut(0) : 0);
    return result;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getInvocationCount() {
    return m_invocationCount;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getSuccessPercentage() {
    return m_successPercentage;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isTest() {
    return true;
  }
  
  private void ppp(String s) {
    System.out.println("[TestNGMethod] " + s);
  }

  private void init(XmlTest xmlTest) {
    m_xmlTest = xmlTest;
    setInvocationNumbers(xmlTest.getInvocationNumbers(
        m_method.getDeclaringClass().getName() + "." + m_method.getName()));
    {
      ITestAnnotation testAnnotation = AnnotationHelper.findTest(getAnnotationFinder(), m_method);
      
      if (testAnnotation == null) {
        // Try on the class
        testAnnotation = AnnotationHelper.findTest(getAnnotationFinder(), m_method.getDeclaringClass());
      }

      if (null != testAnnotation) {
        m_timeOut = testAnnotation.getTimeOut();
        m_successPercentage = testAnnotation.getSuccessPercentage();

        setInvocationCount(testAnnotation.getInvocationCount());
        setThreadPoolSize(testAnnotation.getThreadPoolSize());
        setAlwaysRun(testAnnotation.getAlwaysRun());
        setDescription(testAnnotation.getDescription());
        setRetryAnalyzer(testAnnotation.getRetryAnalyzer());
        setSkipFailedInvocations(testAnnotation.skipFailedInvocations());
        setInvocationTimeOut(testAnnotation.invocationTimeOut());
        setIgnoreMissingDependencies(testAnnotation.ignoreMissingDependencies());
        setPriority(testAnnotation.getPriority());
      }

      // Groups
      {
        initGroups(ITestAnnotation.class);
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  public int getThreadPoolSize() {
    return m_threadPoolSize;
  }
  
  /**
   * Sets the number of threads on which this method should be invoked.
   */
  public void setThreadPoolSize(int threadPoolSize) {
    m_threadPoolSize = threadPoolSize;
  }
  
  /**
   * Sets the number of invocations for this method.
   */
  public void setInvocationCount(int counter) {
    m_invocationCount= counter;
  }
  
  public XmlTest getXmlTest() {
      return m_xmlTest;
  }
  
  /**
   * Clones the current <code>TestNGMethod</code> and its @BeforeMethod and @AfterMethod methods.
   * @see org.testng.internal.BaseTestMethod#clone()
   */
  public TestNGMethod clone() {
    TestNGMethod clone= new TestNGMethod(getMethod(), getAnnotationFinder(), false, getXmlTest());
    ITestClass tc= getTestClass();
    NoOpTestClass testClass= new NoOpTestClass(tc);
    testClass.setBeforeTestMethods(clone(tc.getBeforeTestMethods()));
    testClass.setAfterTestMethod(clone(tc.getAfterTestMethods()));
    clone.m_testClass= testClass;
    clone.setDate(getDate());
    clone.setGroups(getGroups());
    clone.setGroupsDependedUpon(getGroupsDependedUpon());
    clone.setMethodsDependedUpon(getMethodsDependedUpon());
    clone.setAlwaysRun(isAlwaysRun());
    clone.m_beforeGroups= getBeforeGroups();
    clone.m_afterGroups= getAfterGroups();
    clone.m_currentInvocationCount= m_currentInvocationCount;
    clone.setMissingGroup(getMissingGroup());
    clone.setThreadPoolSize(getThreadPoolSize());
    clone.setDescription(getDescription());
    clone.setParameterInvocationCount(getParameterInvocationCount());
    clone.setInvocationCount(getInvocationCount());
    clone.m_successPercentage = getSuccessPercentage();
    clone.m_timeOut = getTimeOut();
    clone.setRetryAnalyzer(getRetryAnalyzer());
    clone.setSkipFailedInvocations(skipFailedInvocations());
    clone.setInvocationNumbers(getInvocationNumbers());

    return clone;
  }
  
  private ITestNGMethod[] clone(ITestNGMethod[] sources) {
    ITestNGMethod[] clones= new ITestNGMethod[sources.length];
    for(int i= 0; i < sources.length; i++) {
      clones[i]= sources[i].clone();
    }
    
    return clones;
  }
  
  /** Sorts ITestNGMethod by Class name. */
  public static final Comparator<ITestNGMethod> SORT_BY_CLASS =
    new Comparator<ITestNGMethod>() {
    
    public int compare(ITestNGMethod o1, ITestNGMethod o2) {
      String c1 = o1.getTestClass().getName();
      String c2 = o2.getTestClass().getName();
      return c1.compareTo(c2);
    }
  };
}
