��һ���ܽ�һ�¹���ͼ�δ�����ص�֪ʶ��Android��2Dͼ�δ���������Ҫ�����¼�����Ҫ������ɣ�

 - Canvas��hold the"draw"calls.     ������ִ�л�ͼ�Ĳ���������ʵ��Canvas�ǻ��ҵĽ�ɫ��Bitmap���������Ļ�����
 - Bitmap��hold the pixels.     ���λͼ����
 - Paint��describe the colors and styles for the drawing      ���ʵ���ʽ����ɫ����ϸ��
 - drawing primtive��   ��ͼ��ԭʼ����

#Canvas
��Ӧ���е���Ҫ���������ͼ����߶���ʱ��Androidϵͳ�ṩ��һ��Canvas�࣬�������ܶ��draw���������԰�ͼ��ֱ�ӻ��Ƶ������󶨵�Bitmap�С�
Canvas�������������Bitmap�󶨲ſ��Խ��в������������еĻ��Ʋ���������Ƶ��󶨵�bitmap���档
��������ʽ���Ի��Canvas��
##new Canvas
���ǿ���ֱ�Ӵ���һ��Canvas����ͬʱ������Ҫ����һ��Bitmap�󶨵�Canvas���С�

```java
Bitmap b = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
Canvas c = new Canvas(b);
```
ͨ��Canvas����Ƶ����ݻᱣ���Bitmap���棬Ȼ�����ǿ���ʹ��Canvas.draw(Bitmap, ...)������Bitmap�е����ݻ��Ƶ���ϵͳ����󶨵�Canvas�С�
##View.onDraw
ͨ���Զ���View����onDraw����������ϵͳ���ṩһ����ʼ����ɵ�Canvas�����ǿ���ֱ�ӵ���draw�������ơ�

```java
class CustomView1 extends View{   
                                                                                                                                  
        ...
                                                                                                                                      
        //���������ǽ�����canvas�ṩ�Ļ���ͼ�η���   
        @Override   
        protected void onDraw(Canvas canvas) {   
          RectF rect = new RectF(0, 0, 100, 100);   
                                                                                                                                  
          canvas.drawArc(rect, //������ʹ�õľ��������С   
                  0,  //��ʼ�Ƕ�   
                  90, //ɨ���ĽǶ�   
                  false, //�Ƿ�ʹ������   
                  paint);                                                                                                                         
        }                                                                                                                         
    }   
```
ʹ��onDraw�ṩ��Canvas����������ֱ�ӵ���canvas.draw()��������draw����ʱʹ��canvas��Ϊ������onDraw������ɺ�ϵͳ���Զ���Canvas���Ƶ����ݼ��ص�view�ϡ�

##on a SurfaceView

```java
public class OpenCvonAndroidGTDforHOGActivity extends Activity 
implements SurfaceHolder.Callback{

    private SurfaceHolder _surfaceHolder;
    private SurfaceView _surfaceView;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        _surfaceView = (SurfaceView)findViewById(R.id.imagesurface);
        _surfaceHolder = _surfaceView.getHolder();
        _surfaceHolder.addCallback(this);
        _surfaceView.setWillNotDraw(false);

    }

    protected void onDraw(Canvas canvas) {
        canvas.drawRGB(255, 0, 255);            
    }

    public void surfaceCreated(SurfaceHolder holder) {
        Canvas canvas = null;
        try {
            canvas = holder.lockCanvas();
            synchronized(holder) {
                onDraw(canvas);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (canvas != null) {
                holder.unlockCanvasAndPost(canvas);
            }
        }
    }
}
```

##����
 
### save() resotre()
 save����������Canvas��״̬��save֮�󣬿��Ե���Canvas��ƽ�ơ���������ת�����С��ü��Ȳ�����
 restore�������ָ�Canvas֮ǰ�����״̬����ֹsave���Canvasִ�еĲ����Ժ����Ļ�����Ӱ�졣
 
 *save��restoreҪ���ʹ�ã�restore���Ա�save�٣������ࣩܶ�����restore���ô�����save�࣬������Error��save��restore֮�䣬�������ӵ��Ƕ�Canvas�����������*

###clipXXX()
�ڵ�ǰ�Ļ�ͼ����ü�(clip)��һ���µĻ�ͼ���������ͼ�������canvas����ĵ�ǰ��ͼ�����ˡ�
###drawXXX()
��һ��������ֵ�ڵ�ǰ��ͼ����ͼ��
###translate(float dx, float dy)
###rotate(float degrees)


#Drawable
*something that can be drawn.*

Drawable����һ���ɻ�����ĳ��󡣲�ͬ����Դ���ʹ����Ų�ͬ��Drawable���͡�Android�������¼���Drawable���ͣ�ColorDrawable��GradientDrawable��BitmapDrawable�� NinePatchDrawable��InsetDrawable��ClipDrawable��ScaleDrawable��RotateDrawable��AnimationDrawable��LayerDrawable��LevelListDrawable��StateListDrawable��TransitionDrawable��

##XML Bitmap
BitmapDrawable �Ƕ�bitmap��һ�ְ�װ��������������װ��bitmap��BitmapDrawable�����ڵĻ��Ʒ�ʽ����ƽ����䡢���������߱���ͼƬԭʼ��С��Ҳ������BitmapDrawable�����ڲ�ʹ��gravityָ���Ķ��뷽ʽ��

```xml
<?xml version="1.0" encoding="utf-8"?>
<bitmap
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:src="@[package:]drawable/drawable_resource"
    android:antialias=["true" | "false"]
    android:dither=["true" | "false"]
    android:filter=["true" | "false"]
    android:gravity=["top" | "bottom" | "left" | "right" | "center_vertical" |
                      "fill_vertical" | "center_horizontal" | "fill_horizontal" |
                      "center" | "fill" | "clip_vertical" | "clip_horizontal"]
    android:mipMap=["true" | "false"]
    android:tileMode=["disabled" | "clamp" | "repeat" | "mirror"] />
```
- android:antialias	    ���Ч��
- android:dither	    ��ͼƬ���غ���Ļ���ز�ƥ��ʱ���Ƿ����ö���Ч��
- android:filter    ����Ч��
- android:gravity λ��
- android:tileMode 

##XML Nine-Patch
Android ƽ̨�����ͼƬ��ʽ �����ͼ������ͼƬ��Ҫ����ʱ�����԰������ǵ�����Ŵ�ͼƬ��ĳ�����֡�

```xml
<?xml version="1.0" encoding="utf-8"?>
<nine-patch
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:src="@[package:]drawable/drawable_resource"
    android:dither=["true" | "false"] />
```

##Layer List
LayerDrawable ����һ��drawable��ÿ��drawable�����ڲ�ͬ�Ĳ㣬�����Ǳ����Ƶ�ʱ�򣬰���˳��ȫ�������Ƶ������ϡ���Ȼ��Щdrawable���н�������ص������򣬵���������λ�ڲ�ͬ�Ĳ㣬�˴�֮�䲻��Ӱ�졣

```xml
<?xml version="1.0" encoding="utf-8"?>
<layer-list
    xmlns:android="http://schemas.android.com/apk/res/android" >
    <item
        android:drawable="@[package:]drawable/drawable_resource"
        android:id="@[+][package:]id/resource_name"
        android:top="dimension"
        android:right="dimension"
        android:bottom="dimension"
        android:left="dimension" />
</layer-list>
```
item�е�DrawableĬ��������view�Ĵ�С��Ϊ����item���ִ�С���������item�����bitmap�ڵ�

```xml
<item>
  <bitmap android:src="@drawable/image"
          android:gravity="center" />
</item>
```
res/drawable/layers.xml:
```xml
<?xml version="1.0" encoding="utf-8"?>
<layer-list xmlns:android="http://schemas.android.com/apk/res/android">
    <item>
      <bitmap android:src="@drawable/android_red"
        android:gravity="center" />
    </item>
    <item android:top="10dp" android:left="10dp">
      <bitmap android:src="@drawable/android_green"
        android:gravity="center" />
    </item>
    <item android:top="20dp" android:left="20dp">
      <bitmap android:src="@drawable/android_blue"
        android:gravity="center" />
    </item>
</layer-list>
```

```xml
<ImageView
    android:layout_height="wrap_content"
    android:layout_width="wrap_content"
    android:src="@drawable/layers" />
```
Ч��ͼ����
![����дͼƬ����](http://img.blog.csdn.net/20150624184627758)

##State List
StateListDrawable����һ��drawable��ÿһ��drawable����Ӧ��һ��״̬��״̬��ѡ��������java�е�switch-case��ϣ�����˳��Ƚ�״̬��������ƥ���״̬�󣬾ͷ��ض�Ӧ��drawable�������Ҫ���ȷ��ƥ���������ǰ�棬���մӾ�ȷ�����Ե�˳�����С�

```xml
<?xml version="1.0" encoding="utf-8"?>
<selector xmlns:android="http://schemas.android.com/apk/res/android"
    android:constantSize=["true" | "false"]
    android:dither=["true" | "false"]
    android:variablePadding=["true" | "false"] >
    <item
        android:drawable="@[package:]drawable/drawable_resource"
        android:state_pressed=["true" | "false"]
        android:state_focused=["true" | "false"]
        android:state_hovered=["true" | "false"]
        android:state_selected=["true" | "false"]
        android:state_checkable=["true" | "false"]
        android:state_checked=["true" | "false"]
        android:state_enabled=["true" | "false"]
        android:state_activated=["true" | "false"]
        android:state_window_focused=["true" | "false"] />
</selector>
```
##Level List
����һ��drawable��ÿһ��drawable����Ӧһ��level��Χ�������Ǳ����Ƶ�ʱ�򣬸���level����ֵѡȡ��Ӧ��һ��drawable���Ƶ������ϡ����õ�android:maxLevel��ֵ���ڻ����setLevel()ֵʱ�������Դ�ͻᱻ���ء�

```xml
<?xml version="1.0" encoding="utf-8"?>
<level-list
    xmlns:android="http://schemas.android.com/apk/res/android" >
    <item
        android:drawable="@drawable/drawable_resource"
        android:maxLevel="integer"
        android:minLevel="integer" />
</level-list>
```

```xml
<?xml version="1.0" encoding="utf-8"?>
<level-list xmlns:android="http://schemas.android.com/apk/res/android" >
    <item
        android:drawable="@drawable/status_off"
        android:maxLevel="0" />
    <item
        android:drawable="@drawable/status_on"
        android:maxLevel="1" />
</level-list>
```

##Transition Drawable
TransitionDrawable ��LayerDrawable�����࣬������ֻ�����������drawable�������ṩ��һ��͸���ȱ仯�Ķ��������Կ��ƴ�һ��drawable���ȵ�����һ��drawable�Ķ���Ч����

```xml
<?xml version="1.0" encoding="utf-8"?>
<transition
xmlns:android="http://schemas.android.com/apk/res/android" >
    <item
        android:drawable="@[package:]drawable/drawable_resource"
        android:id="@[+][package:]id/resource_name"
        android:top="dimension"
        android:right="dimension"
        android:bottom="dimension"
        android:left="dimension" />
</transition>
```

EXAMPLE��

```XML
<?xml version="1.0" encoding="utf-8"?>
<transition xmlns:android="http://schemas.android.com/apk/res/android">
    <item android:drawable="@drawable/on" />
    <item android:drawable="@drawable/off" />
</transition>
```

```xml
<ImageButton
    android:id="@+id/button"
    android:layout_height="wrap_content"
    android:layout_width="wrap_content"
    android:src="@drawable/transition" />
```

```java
ImageButton button = (ImageButton) findViewById(R.id.button);
TransitionDrawable drawable = (TransitionDrawable) button.getDrawable();
drawable.startTransition(500);
```
##Inset Drawable
InsetDrawable ��ʾһ��drawableǶ�뵽����һ��drawable�ڲ����������ڲ���һЩ��࣬��һ�����drawable��padding���ԣ��������� padding��ʾdrawable��������drawable����ı߾࣬insetDrawable��ʾ����drawable������֮��ı߾ࡣ���ؼ���Ҫ�ı�����ʵ�ʵı߿�С��ʱ��Ƚ��ʺ�ʹ��InsetDrawable��

```xml
<?xml version="1.0" encoding="utf-8"?>
<inset
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:drawable="@drawable/drawable_resource"
    android:insetTop="dimension"
    android:insetRight="dimension"
    android:insetBottom="dimension"
    android:insetLeft="dimension" />
```

##Clip Drawable
ClipDrawable �Ƕ�һ��Drawable���м��в��������Կ������drawable�ļ��������Լ�������������Ķ��뷽ʽ��android�еĽ���������ʹ��һ��ClipDrawableʵ��Ч���ģ�������level������ֵ��������������Ĵ�С��

```xml
<?xml version="1.0" encoding="utf-8"?>
<clip
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:drawable="@drawable/drawable_resource"
    android:clipOrientation=["horizontal" | "vertical"]
    android:gravity=["top" | "bottom" | "left" | "right" | "center_vertical" |
                     "fill_vertical" | "center_horizontal" | "fill_horizontal" |
                     "center" | "fill" | "clip_vertical" | "clip_horizontal"] />
```

���ӣ�

```xml
<?xml version="1.0" encoding="utf-8"?>
<clip xmlns:android="http://schemas.android.com/apk/res/android"
    android:drawable="@drawable/android"
    android:clipOrientation="horizontal"
    android:gravity="left" />
```

```xml
<?xml version="1.0" encoding="utf-8"?>
<clip xmlns:android="http://schemas.android.com/apk/res/android"
    android:drawable="@drawable/android"
    android:clipOrientation="horizontal"
    android:gravity="left" />
```

```java
ImageView imageview = (ImageView) findViewById(R.id.image);
ClipDrawable drawable = (ClipDrawable) imageview.getDrawable();
drawable.setLevel(drawable.getLevel() + 1000);
```
![����дͼƬ����](http://img.blog.csdn.net/20150624184435156)

��Ҫע�����ClipDrawable�Ǹ���level�Ĵ�С����ͼƬ���в����ģ��ٷ��ĵ���note���ᵽ��The drawable is clipped completely and not visible when the level is 0 and fully revealed when the level is 10,000��Ҳ����level�Ĵ�С��0��10000��levelΪ0ʱ��ȫ����ʾ��Ϊ10000ʱ��ȫ��ʾ������Drawable�ṩ��setLevel��int level�����������ü�������

##Scale Drawable
ScaleDrawable�Ƕ�һ��Drawable�������Ų��������Ը���level���Կ������drawable�����ű��ʣ�Ҳ�����������������еĶ��뷽ʽ��

```xml
<?xml version="1.0" encoding="utf-8"?>
<scale
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:drawable="@drawable/drawable_resource"
    android:scaleGravity=["top" | "bottom" | "left" | "right" | "center_vertical" |
                          "fill_vertical" | "center_horizontal" | "fill_horizontal" |
                          "center" | "fill" | "clip_vertical" | "clip_horizontal"]
    android:scaleHeight="percentage"
    android:scaleWidth="percentage" />
```

```xml
<?xml version="1.0" encoding="utf-8"?>
<scale xmlns:android="http://schemas.android.com/apk/res/android"
    android:drawable="@drawable/logo"
    android:scaleGravity="center_vertical|center_horizontal"
    android:scaleHeight="80%"
    android:scaleWidth="80%" />
```

##Shape Drawable
���붯̬����άͼ�Σ�ShapeDrawable�����ǿ���������ʵ�ѡ��ʹ��ShapeDrawable����������⻭��ԭʼ����״����Ӧ�õ��κη��

```xml
<?xml version="1.0" encoding="utf-8"?>
<shape
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape=["rectangle" | "oval" | "line" | "ring"] >
    <corners
        android:radius="integer"
        android:topLeftRadius="integer"
        android:topRightRadius="integer"
        android:bottomLeftRadius="integer"
        android:bottomRightRadius="integer" />
    <gradient
        android:angle="integer"
        android:centerX="integer"
        android:centerY="integer"
        android:centerColor="integer"
        android:endColor="color"
        android:gradientRadius="integer"
        android:startColor="color"
        android:type=["linear" | "radial" | "sweep"]
        android:useLevel=["true" | "false"] />
    <padding
        android:left="integer"
        android:top="integer"
        android:right="integer"
        android:bottom="integer" />
    <size
        android:width="integer"
        android:height="integer" />
    <solid
        android:color="color" />
    <stroke
        android:width="integer"
        android:color="color"
        android:dashWidth="integer"
        android:dashGap="integer" />
</shape>
```


#Bitmap
λͼ��ͼƬ��Դ�Ķ�������ʽ������Bitmap���С����Զ�ͼƬ���м��ã���ת�����ŵȲ�����

##��ȡͼƬ��Դ
###ʹ��BitmapDrawable
ʹ��BitmapDrawable (InputStream is)����һ��BitmapDrawable��
ʹ��BitmapDrawable���getBitmap()��ȡ�õ�λͼ��
```java
// ��ȡInputStream���õ�λͼ
InputStream is=res.openRawResource(R.drawable.pic180);
BitmapDrawable bmpDraw=new BitmapDrawable(is);
Bitmap bmp=bmpDraw.getBitmap();
```

###ʹ��BitmapFactory
Bitmap��Ĺ��캯����˽�еģ����沢����ʵ������ϵͳ�������ṩ��һ��BitmapFactory�����ࡣ
BitmapFactory�ж��ֶ�ȡͼƬ��������������ԴID��·�����ļ����������ȶ�����ʽ��ȡλͼ��

```java
static Bitmap decodeByteArray(byte[] data, int offset, int length, BitmapFactory.Options opts);
static Bitmap decodeByteArray(byte[] data, int offset, int length);
static Bitmap decodeFile(String pathName);
static Bitmap decodeFile(String pathName, BitmapFactory.Options opts);
static Bitmap decodeFileDescriptor(FileDescriptor fd);
static Bitmap decodeFileDescriptor(FileDescriptor fd, Rect outPadding, BitmapFactory.Options opts);
static Bitmap decodeResource(Resources res, int id, BitmapFactory.Options opts);
static Bitmap decodeResource(Resources res, int id);
static Bitmap decodeResourceStream(Resources res, TypedValue value, InputStream is, Rect pad, BitmapFactory.Options opts);
static Bitmap decodeStream(InputStream is);
static Bitmap decodeStream(InputStream is, Rect outPadding, BitmapFactory.Options opts);
```

###BitmapFacotry.Options
��ʹ��BitmapFactory.decode����ʱ����ʹ��Optionѡ�

 - inPreferredConfig: ָ��decode���ڴ��У��ֻ��������õı��룬��ѡֵ������Bitmap.Config�С�ȱʡֵ��ARGB_8888��
 - inJustDecodeBounds: �������Ϊtrue������ȡͼƬ���������ڴ棬���᷵��ͼƬ�ĸ߿����Ϣ��
 - inSampleSize: ����decodeʱ�����ű�����
 - outHeight : ��ȡͼƬ�ĸ߶�
 - outWidth :    ��ȡͼƬ�Ŀ��
 - inDensity:    λͼ������ѹ����
 - TargetDensity:����Ŀ��λͼ������ѹ���ȣ�Ҫ���ɵ�λͼ��
 - inScaled:����Ϊtrueʱ����ͼƬѹ������inDensity��inTargetDensity��
��ȡһ���ļ�·���õ�һ��λͼ�����ָ���ļ�Ϊ�ջ��߲��ܽ�����ļ����򷵻�NULL��

###���ش�ͼƬ������ͼ��
�����Ǽ��ش�ͼƬʱ����������Out of Memory Error����ʱ������Ӧ���ʵ�ѹ��ͼƬ����ء�BitmapFactory.Options����inJustDecodeBoundsΪtrue����ʹ��decodeFile()�ȷ����������ڲ�����ռ�״̬�¼����ͼƬ�Ĵ�С��

```java
ImageView largeImage = (ImageView) findViewById(R.id.imageView1);
/*�����Ļ������*/
Display display = getWindowManager().getDefaultDisplay();
int displayWidth = display.getWidth();  
/*��ȡԭʼͼƬ���ش�С*/
BitmapFactory.Options options = new BitmapFactory.Options();
options.inJustDecodeBounds = true; //������ռ�״̬�¼����ͼƬ�Ĵ�С
BitmapFactory.decodeResource(getResources(), R.drawable.largeimage, options);
int width = options.outWidth;
/*������Ļ������غ�ͼƬ�������������ѹ������*/
if (width > displayWidth) {
  int widthRatio = Math.round((float) width / (float) displayWidth);
  options.inSampleSize = widthRatio;
}
options.inJustDecodeBounds = false;
Bitmap scaledBitmap =  BitmapFactory.decodeResource(getResources(), R.drawable.largeimage, options);
largeImage.setImageBitmap(scaledBitmap);
```
##��ʾλͼ
ת��ΪBitmapDrawable������ʾλͼ

```java
 // ��ȡλͼ
 Bitmap bmp=BitmapFactory.decodeResource(res, R.drawable.pic180);
 // ת��ΪBitmapDrawable����
 BitmapDrawable bmpDraw=new BitmapDrawable(bmp);
 // ��ʾλͼ
 ImageView iv2 = (ImageView)findViewById(R.id.ImageView02);
 iv2.setImageDrawable(bmpDraw);
```
ʹ��Canvas����ʾλͼ

```java
class Panel extends View{     	  
        public Panel(Context context) {  
            super(context); 
        }      
        public void onDraw(Canvas canvas){  
            Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.pic180);  
            canvas.drawColor(Color.BLACK);  
            canvas.drawBitmap(bmp, 10, 10, null);  
        }  
    } 
```
##λͼ������Matrix 
λͼ�����ţ���Android SDK���ṩ��2�ַ�����

 1. ��һ��λͼ���������ػ�һ�飬�����λͼ����������Ҫ���ˣ���λͼ����ʾ����һ����
 `  drawBitmap(Bitmap bitmap, Rect src, Rect dst, Paint paint);`
 



 2. ��ԭ��λͼ�Ļ����ϣ�����ԭλͼ������һ���µ�λͼ��
```java
public static Bitmap createBitmap(Bitmap source, int x, int y, intwidth, int height,
            Matrix m, boolean filter)
public static Bitmap createBitmap(Bitmap source, int x, int y, intwidth, int height)
public static Bitmap createScaledBitmap(Bitmap src, int dstWidth,
            int dstHeight,boolean filter)
```
��һ�����������յ�ʵ�֣�������ֻ�ǶԵ�һ�ַ����ķ�װ��
�ڶ����������Դ�ԴBitmap��ָ������(x,y, width, height)���ڳ�һ����ʵ�ּ��У��������������԰�ԴBitmap����ΪdstWidth X dstHeight��Bitmap��

```java
Bitmap bmp =BitmapFactory.decodeResource(getResources(), R.drawable.pic180);   
Matrix matrix=new Matrix();   
matrix.postScale(0.2f,0.2f);   
Bitmapdstbmp=Bitmap.createBitmap(bmp,0,0,bmp.getWidth(),
bmp.getHeight(),matrix,true);   
canvas.drawColor(Color.BLACK);     
canvas.drawBitmap(dstbmp,10, 10, null); 
```
MatrixΪ�������˼��һ��������Bitmap��ϣ�ʵ��ͼ������š����Ρ�Ť���Ȳ�����
Matrix�ṩ��һЩ����������ͼƬ�任��

```
setTranslate(float dx,float dy);    //����Matrix����λ�ơ�
setSkew(float kx,float ky);    //����Matrix������б��kx��kyΪX��Y�����ϵı�����
setSkew(float kx,float ky,float px,float py);    //����Matrix��px��pyΪ���Ľ�����б��kx��kyΪX��Y�����ϵ���б������
setRotate(float degrees);    //����Matrix����depress�Ƕȵ���ת������Ϊ��0,0����
setRotate(float degrees,float px,float py);    //����Matrix����depress�Ƕȵ���ת������Ϊ(px,py)��
setScale(float sx,float sy);    //����Matrix�������ţ�sx��syΪX��Y�����ϵ����ű�����
setScale(float sx,float sy,float px,float py);    //����Matrix��(px,py)Ϊ���Ľ������ţ�sx��syΪX��Y�����ϵ����ű�����
```

���м�����Ҫ����ע��һ�£�

- ����һ����BitmapFactory.decodeXxx()�������ص�Bitmap������ԣ�����һ��ֻ���ģ��޷�������д�������ʹ��Bitmap.createBitmap()�������´���һ��Bitmap����Ŀ������ſ��ԶԿ�����Bitmap���д���
- ��Ϊͼ��ı任�����ÿһ�����ص�ģ�������Щ�任���ܷ������ص�Ķ�ʧ��������Ҫʹ��Paint.setAnitiAlias(boolean)������������ݣ�����ͼƬ�任���Ч����úܶࡣ
- �����´���һ��Bitmap����Ŀ�����ʱ����Ҫע�����Ŀ�ߣ�������ò��ף��ܿ��ܱ任������ص��Ѿ��ƶ���"ͼƬ֮��"ȥ�ˡ�