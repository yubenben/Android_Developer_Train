这一节总结一下关于图形处理相关的知识。Android中2D图形处理引擎主要有以下几个重要部分组成：

 - Canvas：hold the"draw"calls.     画布，执行画图的操作函数（实际Canvas是画家的角色，Bitmap才是真正的画布）
 - Bitmap：hold the pixels.     存放位图数据
 - Paint：describe the colors and styles for the drawing      画笔的样式（颜色，粗细）
 - drawing primtive：   绘图的原始内容

#Canvas
在应用中当需要绘制特殊的图像或者动画时，Android系统提供了一个Canvas类，他包含很多的draw函数，可以把图象直接绘制到与界面绑定的Bitmap中。
Canvas（画布）必须和Bitmap绑定才可以进行操作，画布所有的绘制操作都会绘制到绑定的bitmap上面。
有三种形式可以获得Canvas类
##new Canvas
我们可以直接创建一个Canvas对象。同时我们需要创建一个Bitmap绑定到Canvas类中。

```java
Bitmap b = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
Canvas c = new Canvas(b);
```
通过Canvas类绘制的内容会保存的Bitmap里面，然后我们可以使用Canvas.draw(Bitmap, ...)函数将Bitmap中的内容绘制到与系统界面绑定的Canvas中。
##View.onDraw
通过自定义View，在onDraw（）函数中系统会提供一个初始化完成的Canvas，我们可以直接调用draw函数绘制。

```java
class CustomView1 extends View{   
                                                                                                                                  
        ...
                                                                                                                                      
        //在这里我们将测试canvas提供的绘制图形方法   
        @Override   
        protected void onDraw(Canvas canvas) {   
          RectF rect = new RectF(0, 0, 100, 100);   
                                                                                                                                  
          canvas.drawArc(rect, //弧线所使用的矩形区域大小   
                  0,  //开始角度   
                  90, //扫过的角度   
                  false, //是否使用中心   
                  paint);                                                                                                                         
        }                                                                                                                         
    }   
```
使用onDraw提供的Canvas参数，我们直接调用canvas.draw()或者其他draw函数时使用canvas作为参数。onDraw函数完成后，系统会自动将Canvas绘制的内容加载到view上。

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

##函数
 
### save() resotre()
 save：用来保存Canvas的状态。save之后，可以调用Canvas的平移、放缩、旋转、错切、裁剪等操作。
 restore：用来恢复Canvas之前保存的状态。防止save后对Canvas执行的操作对后续的绘制有影响。
 
 *save和restore要配对使用（restore可以比save少，但不能多），如果restore调用次数比save多，会引发Error。save和restore之间，往往夹杂的是对Canvas的特殊操作。*

###clipXXX()
在当前的画图区域裁剪(clip)出一个新的画图区域，这个画图区域就是canvas对象的当前画图区域了。
###drawXXX()
以一定的坐标值在当前画图区域画图。
###translate(float dx, float dy)
###rotate(float degrees)


#Drawable
*something that can be drawn.*

Drawable就是一个可画对象的抽象。不同的资源类型代表着不同的Drawable类型。Android内置以下几种Drawable类型：ColorDrawable、GradientDrawable、BitmapDrawable、 NinePatchDrawable、InsetDrawable、ClipDrawable、ScaleDrawable、RotateDrawable、AnimationDrawable、LayerDrawable、LevelListDrawable、StateListDrawable、TransitionDrawable。

##XML Bitmap
BitmapDrawable 是对bitmap的一种包装，可以设置它包装的bitmap在BitmapDrawable区域内的绘制方式，如平铺填充、拉伸填充或者保持图片原始大小，也可以在BitmapDrawable区域内部使用gravity指定的对齐方式。

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
- android:antialias	    锯齿效果
- android:dither	    当图片像素和屏幕像素不匹配时，是否启用抖动效果
- android:filter    过滤效果
- android:gravity 位置
- android:tileMode 

##XML Nine-Patch
Android 平台特殊的图片格式 “点九图“，当图片需要伸缩时，可以按照我们的需求放大图片的某个部分。

```xml
<?xml version="1.0" encoding="utf-8"?>
<nine-patch
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:src="@[package:]drawable/drawable_resource"
    android:dither=["true" | "false"] />
```

##Layer List
LayerDrawable 管理一组drawable，每个drawable都处于不同的层，当它们被绘制的时候，按照顺序全部都绘制到画布上。虽然这些drawable会有交差或者重叠的区域，但是它们是位于不同的层，彼此之间不会影响。

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
item中的Drawable默认伸缩到view的大小，为了让item保持大小不变可以在item中添加bitmap节点

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
效果图如下
![这里写图片描述](http://img.blog.csdn.net/20150624184627758)

##State List
StateListDrawable管理一组drawable，每一个drawable都对应着一组状态，状态的选择类似于java中的switch-case组合，按照顺序比较状态，当遇到匹配的状态后，就返回对应的drawable，因此需要把最精确的匹配放置在最前面，按照从精确到粗略的顺序排列。

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
管理一组drawable，每一个drawable都对应一个level范围，当它们被绘制的时候，根据level属性值选取对应的一个drawable绘制到画布上。设置的android:maxLevel的值大于或等于setLevel()值时，这个资源就会被加载。

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
TransitionDrawable 是LayerDrawable的子类，不过它只负责管理两层drawable，并且提供了一个透明度变化的动画，可以控制从一层drawable过度到另外一层drawable的动画效果。

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

EXAMPLE：

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
InsetDrawable 表示一个drawable嵌入到另外一个drawable内部，并且在内部留一些间距，这一点很像drawable的padding属性，区别在于 padding表示drawable的内容与drawable本身的边距，insetDrawable表示两个drawable和容器之间的边距。当控件需要的背景比实际的边框小的时候比较适合使用InsetDrawable。

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
ClipDrawable 是对一个Drawable进行剪切操作，可以控制这个drawable的剪切区域，以及相相对于容器的对齐方式，android中的进度条就是使用一个ClipDrawable实现效果的，它根据level的属性值，决定剪切区域的大小。

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

例子：

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
![这里写图片描述](http://img.blog.csdn.net/20150624184435156)

需要注意的是ClipDrawable是根据level的大小控制图片剪切操作的，官方文档的note中提到：The drawable is clipped completely and not visible when the level is 0 and fully revealed when the level is 10,000。也就是level的大小从0到10000，level为0时完全不显示，为10000时完全显示。是用Drawable提供的setLevel（int level）方法来设置剪切区域。

##Scale Drawable
ScaleDrawable是对一个Drawable进行缩放操作，可以根据level属性控制这个drawable的缩放比率，也可以设置它在容器中的对齐方式。

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
当想动态画二维图形，ShapeDrawable对象是可能是你合适的选择．使用ShapeDrawable，你可以随意画出原始的形状并且应用到任何风格．

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
位图，图片资源的二进制形式保存在Bitmap类中。可以对图片进行剪裁，旋转，缩放等操作。

##获取图片资源
###使用BitmapDrawable
使用BitmapDrawable (InputStream is)构造一个BitmapDrawable；
使用BitmapDrawable类的getBitmap()获取得到位图；
```java
// 读取InputStream并得到位图
InputStream is=res.openRawResource(R.drawable.pic180);
BitmapDrawable bmpDraw=new BitmapDrawable(is);
Bitmap bmp=bmpDraw.getBitmap();
```

###使用BitmapFactory
Bitmap类的构造函数是私有的，外面并不能实例化，系统给我们提供了一个BitmapFactory构造类。
BitmapFactory有多种读取图片方法，他可以资源ID、路径、文件、数据流等多种形式获取位图。

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
在使用BitmapFactory.decode函数时，会使用Option选项。

 - inPreferredConfig: 指定decode到内存中，手机中所采用的编码，可选值定义在Bitmap.Config中。缺省值是ARGB_8888。
 - inJustDecodeBounds: 如果设置为true，不获取图片，不分配内存，但会返回图片的高宽度信息。
 - inSampleSize: 设置decode时的缩放比例。
 - outHeight : 获取图片的高度
 - outWidth :    获取图片的宽度
 - inDensity:    位图的像素压缩比
 - TargetDensity:用于目标位图的像素压缩比（要生成的位图）
 - inScaled:设置为true时进行图片压缩，从inDensity到inTargetDensity。
读取一个文件路径得到一个位图。如果指定文件为空或者不能解码成文件，则返回NULL。

###加载大图片（缩略图）
当我们加载大图片时，经常出现Out of Memory Error。这时候我们应该适当压缩图片后加载。BitmapFactory.Options设置inJustDecodeBounds为true后，再使用decodeFile()等方法，可以在不分配空间状态下计算出图片的大小。

```java
ImageView largeImage = (ImageView) findViewById(R.id.imageView1);
/*获得屏幕的像素*/
Display display = getWindowManager().getDefaultDisplay();
int displayWidth = display.getWidth();  
/*读取原始图片像素大小*/
BitmapFactory.Options options = new BitmapFactory.Options();
options.inJustDecodeBounds = true; //不分配空间状态下计算出图片的大小
BitmapFactory.decodeResource(getResources(), R.drawable.largeimage, options);
int width = options.outWidth;
/*根据屏幕宽度像素和图片宽度像素来决定压缩比例*/
if (width > displayWidth) {
  int widthRatio = Math.round((float) width / (float) displayWidth);
  options.inSampleSize = widthRatio;
}
options.inJustDecodeBounds = false;
Bitmap scaledBitmap =  BitmapFactory.decodeResource(getResources(), R.drawable.largeimage, options);
largeImage.setImageBitmap(scaledBitmap);
```
##显示位图
转换为BitmapDrawable对象显示位图

```java
 // 获取位图
 Bitmap bmp=BitmapFactory.decodeResource(res, R.drawable.pic180);
 // 转换为BitmapDrawable对象
 BitmapDrawable bmpDraw=new BitmapDrawable(bmp);
 // 显示位图
 ImageView iv2 = (ImageView)findViewById(R.id.ImageView02);
 iv2.setImageDrawable(bmpDraw);
```
使用Canvas类显示位图

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
##位图缩放与Matrix 
位图的缩放，在Android SDK中提供了2种方法：

 1. 将一个位图按照需求重画一遍，画后的位图就是我们需要的了，与位图的显示几乎一样。
 `  drawBitmap(Bitmap bitmap, Rect src, Rect dst, Paint paint);`
 



 2. 在原有位图的基础上，缩放原位图，创建一个新的位图：
```java
public static Bitmap createBitmap(Bitmap source, int x, int y, intwidth, int height,
            Matrix m, boolean filter)
public static Bitmap createBitmap(Bitmap source, int x, int y, intwidth, int height)
public static Bitmap createScaledBitmap(Bitmap src, int dstWidth,
            int dstHeight,boolean filter)
```
第一个方法是最终的实现，后两种只是对第一种方法的封装。
第二个方法可以从源Bitmap中指定区域(x,y, width, height)中挖出一块来实现剪切；第三个方法可以把源Bitmap缩放为dstWidth X dstHeight的Bitmap。

```java
Bitmap bmp =BitmapFactory.decodeResource(getResources(), R.drawable.pic180);   
Matrix matrix=new Matrix();   
matrix.postScale(0.2f,0.2f);   
Bitmapdstbmp=Bitmap.createBitmap(bmp,0,0,bmp.getWidth(),
bmp.getHeight(),matrix,true);   
canvas.drawColor(Color.BLACK);     
canvas.drawBitmap(dstbmp,10, 10, null); 
```
Matrix为矩阵的意思，一般用来与Bitmap配合，实现图像的缩放、变形、扭曲等操作。
Matrix提供了一些方法来控制图片变换：

```
setTranslate(float dx,float dy);    //控制Matrix进行位移。
setSkew(float kx,float ky);    //控制Matrix进行倾斜，kx、ky为X、Y方向上的比例。
setSkew(float kx,float ky,float px,float py);    //控制Matrix以px、py为轴心进行倾斜，kx、ky为X、Y方向上的倾斜比例。
setRotate(float degrees);    //控制Matrix进行depress角度的旋转，轴心为（0,0）。
setRotate(float degrees,float px,float py);    //控制Matrix进行depress角度的旋转，轴心为(px,py)。
setScale(float sx,float sy);    //设置Matrix进行缩放，sx、sy为X、Y方向上的缩放比例。
setScale(float sx,float sy,float px,float py);    //设置Matrix以(px,py)为轴心进行缩放，sx、sy为X、Y方向上的缩放比例。
```

还有几点需要额外注意一下：

- 对于一个从BitmapFactory.decodeXxx()方法加载的Bitmap对象而言，它是一个只读的，无法对其进行处理，必须使用Bitmap.createBitmap()方法重新创建一个Bitmap对象的拷贝，才可以对拷贝的Bitmap进行处理。
- 因为图像的变换是针对每一个像素点的，所以有些变换可能发生像素点的丢失，这里需要使用Paint.setAnitiAlias(boolean)设置来消除锯齿，这样图片变换后的效果会好很多。
- 在重新创建一个Bitmap对象的拷贝的时候，需要注意它的宽高，如果设置不妥，很可能变换后的像素点已经移动到"图片之外"去了。