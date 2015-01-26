package com.sample.scrollmenu;

import java.io.IOException;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.FillResolutionPolicy;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.ParallaxBackground;
import org.andengine.entity.scene.background.ParallaxBackground.ParallaxEntity;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.sprite.TiledSprite;
import org.andengine.entity.util.FPSLogger;
import org.andengine.input.touch.TouchEvent;
import org.andengine.input.touch.detector.ClickDetector;
import org.andengine.input.touch.detector.ClickDetector.IClickDetectorListener;
import org.andengine.input.touch.detector.ScrollDetector;
import org.andengine.input.touch.detector.ScrollDetector.IScrollDetectorListener;
import org.andengine.input.touch.detector.SurfaceScrollDetector;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.atlas.bitmap.BuildableBitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.source.IBitmapTextureAtlasSource;
import org.andengine.opengl.texture.atlas.buildable.builder.BlackPawnTextureAtlasBuilder;
import org.andengine.opengl.texture.atlas.buildable.builder.ITextureAtlasBuilder.TextureAtlasBuilderException;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.ui.activity.BaseGameActivity;
import org.andengine.util.debug.Debug;

import android.util.Log;

public class ScrollMenuGameActivity extends BaseGameActivity implements IScrollDetectorListener, IOnSceneTouchListener, IClickDetectorListener {
	
	BuildableBitmapTextureAtlas menuAtlas;
	TiledTextureRegion menuGameTexture;
	TiledTextureRegion menuAboutTexture;
	TiledTextureRegion menuHowToTexture;
	TiledTextureRegion menuProgressTexture;
	TiledTextureRegion menuExitTexture;
	
	BitmapTextureAtlas leftRightAtlas;
	ITextureRegion leftTexture;
	ITextureRegion rightTexture;
	
	BitmapTextureAtlas bgAtlas;
	ITextureRegion bgTexture;
	
	Sprite menuLeft;
	Sprite menuRight;
	
	SurfaceScrollDetector scrollDetector;
	ClickDetector clickDetector;
	
	int PADDING = 30;
	
	float minX = 0;
	float maxX = 0;
	float currentX = 0;
	int itemClicked = -1;
	
	Rectangle scrollBar;
	
	Camera camera;
	
	Scene scene;

	static final int CAMERA_WIDTH = 800;
	static final int CAMERA_HEIGHT = 480;
	
	TiledSprite[] tiledSprite;
	@Override
	public EngineOptions onCreateEngineOptions() {
		camera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
		EngineOptions engineOptions = new EngineOptions(true, ScreenOrientation.LANDSCAPE_FIXED, new FillResolutionPolicy(), camera);
		return engineOptions;
	}

	@Override
	public void onCreateResources(OnCreateResourcesCallback pOnCreateResourcesCallback) throws IOException {
		loadLeftRightResources();
		loadMenuResources();
		loadBGResources();
		
		pOnCreateResourcesCallback.onCreateResourcesFinished();
	}
	
	private void loadMenuResources() {
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
		menuAtlas = new BuildableBitmapTextureAtlas(getTextureManager(), 1024, 1024, TextureOptions.BILINEAR);
		menuGameTexture = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(menuAtlas, this, "games_btn.png", 2, 1);
		menuAboutTexture = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(menuAtlas, this, "about_btn.png", 2, 1);
		menuHowToTexture = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(menuAtlas, this, "htp_btn.png", 2, 1);
		menuProgressTexture = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(menuAtlas, this, "prog_btn.png", 2, 1);
		menuExitTexture = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(menuAtlas, this, "quit_btn.png", 2, 1);
		try {
			menuAtlas.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource, BitmapTextureAtlas>(0, 1, 0));
			menuAtlas.load();
		} catch(final TextureAtlasBuilderException e) {
			Debug.e(e);
		}
	}
	
	private void loadLeftRightResources() {
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
		leftRightAtlas = new BitmapTextureAtlas(getTextureManager(), 256, 256);
		leftTexture = BitmapTextureAtlasTextureRegionFactory.createFromAsset(leftRightAtlas, this, "menu_left.png", 0,0);
		rightTexture = BitmapTextureAtlasTextureRegionFactory.createFromAsset(leftRightAtlas, this, "menu_right.png", 70,0);
		leftRightAtlas.load();
	}
	
	private void loadBGResources() {
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
		bgAtlas = new BitmapTextureAtlas(getTextureManager(), 1024, 1024);
		bgTexture = BitmapTextureAtlasTextureRegionFactory.createFromAsset(bgAtlas, this, "menu_bg.png", 0,0);
		bgAtlas.load();
	}

	@Override
	public void onCreateScene(OnCreateSceneCallback pOnCreateSceneCallback) throws IOException {
		this.mEngine.registerUpdateHandler(new FPSLogger());
		
		this.scene = new Scene();
		//this.scene.setBackground(new Background(.46f, .33f, .14f));
		
		this.scrollDetector = new SurfaceScrollDetector(this);
		this.clickDetector = new ClickDetector(this);

		this.scene.setOnSceneTouchListener(this);
		this.scene.setTouchAreaBindingOnActionDownEnabled(true);
		this.scene.setTouchAreaBindingOnActionMoveEnabled(true);
		
		pOnCreateSceneCallback.onCreateSceneFinished(scene);
	}

	@Override
	public void onPopulateScene(Scene pScene,OnPopulateSceneCallback pOnPopulateSceneCallback) throws IOException {	
		createBackground();
		createMenuBoxes();
		pOnPopulateSceneCallback.onPopulateSceneFinished();
	}
	
	
	@Override
	public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
		this.clickDetector.onTouchEvent(pSceneTouchEvent);
		this.scrollDetector.onTouchEvent(pSceneTouchEvent);
		
		scene.sortChildren();
		return true;
	}
	
	@Override
	public void onScroll(ScrollDetector pScollDetector, int pPointerID, float pDistanceX, float pDistanceY) {
		//Disable the menu arrows left and right (15px padding)
    	if(camera.getXMin()<=15)
         	menuLeft.setVisible(false);
         else
         	menuLeft.setVisible(true);
    	 
    	 if(camera.getXMin()>maxX-15)
             menuRight.setVisible(false);
         else
        	 menuRight.setVisible(true);
         	
        //Return if ends are reached
        if ( ((currentX - pDistanceX) < minX)  ){                	
            return;
        } else if((currentX - pDistanceX) > maxX){
        	
        	return;
        }
        
        //Center camera to the current point
        this.camera.offsetCenter(-pDistanceX, 0);
        currentX -= pDistanceX;
        
        /*
        //Set the scrollbar with the camera
        float tempX = camera.getCenterX() - CAMERA_WIDTH/2;
        // add the % part to the position
        tempX+= (tempX/(maxX+CAMERA_WIDTH))*CAMERA_WIDTH;      
        //set the position
        scrollBar.setPosition(tempX, scrollBar.getY());
        */
        
        //set the arrows for left and right
        menuLeft.setPosition(camera.getCenterX()-CAMERA_WIDTH/2 + 45 ,200);
        menuRight.setPosition(camera.getCenterX()+CAMERA_WIDTH/2 - 45, 200);
       
        //Because Camera can have negativ X values, so set to 0
    	if(this.camera.getXMin()<0){
    		this.camera.offsetCenter(0,0);
    		currentX = 0;
    	} 
		
	}

	@Override
	public void onClick(ClickDetector pClickDetector, int pPointerID,float pSceneX, float pSceneY) {
		
	}
	
	private void createMenuBoxes() {
		int spriteX = 150;
		int spriteY = 200;
		
		int item = 1;
		tiledSprite = new TiledSprite[5];
		TiledTextureRegion[] menuTextureArray = {menuGameTexture, menuAboutTexture, menuHowToTexture, menuProgressTexture, menuExitTexture};
		
		for(int ctr = 0; ctr < menuTextureArray.length; ctr++) {
			final int finalIndex = ctr;
			final int itemToLoad = item;
			
			tiledSprite[ctr] = new TiledSprite(spriteX, spriteY, menuTextureArray[ctr], getVertexBufferObjectManager()) {
				@Override
				public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
					switch(pSceneTouchEvent.getAction()) {
					case TouchEvent.ACTION_DOWN:
						tiledSprite[finalIndex].setScale(0.9f);
						tiledSprite[finalIndex].setCurrentTileIndex(1);
						break;
					case TouchEvent.ACTION_UP:
						tiledSprite[finalIndex].setScale(1.0f);
						tiledSprite[finalIndex].setCurrentTileIndex(0);
						
						Log.d("position", "menuLeft " + (camera.getCenterX() - CAMERA_WIDTH/2 +45));
						Log.d("position", "menuRight " + (camera.getCenterX()+CAMERA_WIDTH/2 - 45));
						break;
					}
					itemClicked = itemToLoad;
					return super.onAreaTouched(pSceneTouchEvent, pTouchAreaLocalX, pTouchAreaLocalY);
					
				}		
			};
			item++;
			
			mEngine.runOnUpdateThread(new Runnable() {
				@Override
				public void run() {
					scene.attachChild(tiledSprite[finalIndex]);
					scene.registerTouchArea(tiledSprite[finalIndex]);
					tiledSprite[finalIndex].setZIndex(0);
					
				}
			});
			
			spriteX += 230;
		}
		
		maxX = spriteX - CAMERA_WIDTH;
		
		// LEFT AND RIGHT OF MENU				
		menuLeft = new Sprite(45, 200,leftTexture, this.getVertexBufferObjectManager());			
		this.scene.attachChild(menuLeft);
		menuLeft.setZIndex(1);
		menuRight = new Sprite(755, 200, rightTexture, this.getVertexBufferObjectManager());			
		this.scene.attachChild(menuRight);	
		menuRight.setZIndex(1);
		menuLeft.setVisible(false);

		// set the scroll bar size			
		//float scrollBarSize = 20;				
		//scrollBar = new Rectangle(10, 10, scrollBarSize, 20, this.getVertexBufferObjectManager());				
		//scrollBar.setColor(Color.BLUE);			
		//this.scene.attachChild(scrollBar);
				
	}
	
	private void createBackground() {
		ParallaxBackground background = new ParallaxBackground(0,0,0);
		background.attachParallaxEntity(new ParallaxEntity(0, new Sprite(400,240, bgTexture, getVertexBufferObjectManager())));
		scene.setBackground(background);
	}

	
	
	@Override
	public void onScrollStarted(ScrollDetector pScollDetector, int pPointerID,
			float pDistanceX, float pDistanceY) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onScrollFinished(ScrollDetector pScollDetector, int pPointerID,
			float pDistanceX, float pDistanceY) {
		// TODO Auto-generated method stub
		
	}
	

}
