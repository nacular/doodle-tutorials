"use strict";(self.webpackChunkwebsite=self.webpackChunkwebsite||[]).push([[6486],{9662:(e,n,i)=>{i.r(n),i.d(n,{assets:()=>m,contentTitle:()=>p,default:()=>f,frontMatter:()=>h,metadata:()=>t,sourceTree:()=>u,toc:()=>g});const t=JSON.parse('{"id":"tabstrip","title":"Animating Tab Strip","description":"TabStrip Tutorial","source":"@site/docs/tabstrip.mdx","sourceDirName":".","slug":"/tabstrip","permalink":"/doodle-tutorials/docs/tabstrip","draft":false,"unlisted":false,"tags":[],"version":"current","frontMatter":{"title":"Animating Tab Strip","hide_title":true},"sidebar":"tutorialSidebar","previous":{"title":"Timed Cards","permalink":"/doodle-tutorials/docs/timedcards"},"next":{"title":"Contacts","permalink":"/doodle-tutorials/docs/contacts"}}');var a=i(4848),o=i(8453),s=(i(4865),i(9365),i(854)),r=i(9053),l=i(7020);const c='@file:OptIn(ExperimentalWasmDsl::class)\n\nimport org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl\n\n//sampleStart\nplugins {\n    kotlin("multiplatform")\n    application\n}\n\nkotlin {\n    js     { browser { binaries.executable() } } // Web     (JS  ) executable\n    wasmJs { browser { binaries.executable()     // Web     (WASM) executable\n        applyBinaryen {}                         // Binary size optimization\n    } }\n    jvm    {                                     // Desktop (JVM ) executable\n        compilations.all {\n            kotlinOptions { jvmTarget = "11" }   // JVM 11 is needed for Desktop\n        }\n        withJava()\n    }\n\n    sourceSets {\n        // Source set for all platforms\n        commonMain.dependencies {\n            api(libs.doodle.animation)\n        }\n\n        // Web (JS) platform source set\n        jsMain.dependencies {\n            implementation(libs.doodle.browser)\n        }\n\n        // Web (WASM) platform source set\n        val wasmJsMain by getting {\n            dependencies {\n                implementation(libs.doodle.browser)\n            }\n        }\n\n        // Desktop (JVM) platform source set\n        jvmMain.dependencies {\n            // helper to derive OS/architecture pair\n            when (osTarget()) {\n                "macos-x64"     -> implementation(libs.doodle.desktop.jvm.macos.x64    )\n                "macos-arm64"   -> implementation(libs.doodle.desktop.jvm.macos.arm64  )\n                "linux-x64"     -> implementation(libs.doodle.desktop.jvm.linux.x64    )\n                "linux-arm64"   -> implementation(libs.doodle.desktop.jvm.linux.arm64  )\n                "windows-x64"   -> implementation(libs.doodle.desktop.jvm.windows.x64  )\n                "windows-arm64" -> implementation(libs.doodle.desktop.jvm.windows.arm64)\n            }\n        }\n    }\n}\n\n// Desktop entry point\napplication {\n    mainClass.set("io.nacular.doodle.examples.MainKt")\n}\n//sampleEnd\n\n// could be moved to buildSrc, but kept here for clarity\nfun osTarget(): String {\n    val osName = System.getProperty("os.name")\n    val targetOs = when {\n        osName == "Mac OS X"       -> "macos"\n        osName.startsWith("Win"  ) -> "windows"\n        osName.startsWith("Linux") -> "linux"\n        else                       -> error("Unsupported OS: $osName")\n    }\n\n    val targetArch = when (val osArch = System.getProperty("os.arch")) {\n        "x86_64", "amd64" -> "x64"\n        "aarch64"         -> "arm64"\n        else              -> error("Unsupported arch: $osArch")\n    }\n\n    return "${targetOs}-${targetArch}"\n}',d="package io.nacular.doodle.examples\n\nimport io.nacular.doodle.animation.Animator\nimport io.nacular.doodle.application.Application\nimport io.nacular.doodle.core.Display\nimport io.nacular.doodle.geometry.PathMetrics\nimport io.nacular.doodle.geometry.Size\nimport io.nacular.doodle.layout.constraints.center\nimport io.nacular.doodle.layout.constraints.constrain\n\n/**\n * Simple app that places a [TabStrip] at the center of the display.\n */\n//sampleStart\nclass TabStripApp(display: Display, animator: Animator, pathMetrics: PathMetrics): Application {\n    init {\n        // creat and display a single TabStrip\n        with(display) {\n            this += TabStrip(animator, pathMetrics).apply {\n                size = Size(375, 100)\n            }\n\n            layout = constrain(first(), center)\n        }\n    }\n\n    override fun shutdown() { /* no-op */ }\n}\n//sampleEnd",h={title:"Animating Tab Strip",hide_title:!0},p=void 0,m={},u=(0,l.a)([{label:"src",children:[{label:"commonMain",children:[{label:"kotlin"}]},{label:"jsMain",children:[{label:"kotlin"},{label:"resources"}]},{label:"jvmMain",children:[{label:"kotlin"}]},{label:"wasmJsMain",children:[{label:"kotlin"},{label:"resources"}]}]},{label:"build.gradle.kts"}]),g=[{value:"Project Setup",id:"project-setup",level:2},{value:"The Application",id:"the-application",level:2},{value:"The TabStrip View",id:"the-tabstrip-view",level:2},{value:"Render Logic",id:"render-logic",level:3},{value:"Triggering The Animation",id:"triggering-the-animation",level:3},{value:"Animation Timeline",id:"animation-timeline",level:3},{value:"Animation Logic",id:"animation-logic",level:3},{value:"Rendering On Animation",id:"rendering-on-animation",level:3},{value:"Potential Improvements",id:"potential-improvements",level:2}];function b(e){const n={a:"a",admonition:"admonition",code:"code",em:"em",h2:"h2",h3:"h3",img:"img",p:"p",strong:"strong",...(0,o.R)(),...e.components};return(0,a.jsxs)(a.Fragment,{children:[(0,a.jsx)("h1",{children:(0,a.jsx)("a",{class:"inline-github-link",href:"https://github.com/nacular/doodle-tutorials/tree/master/TabStrip",target:"_blank",children:"TabStrip Tutorial"})}),"\n",(0,a.jsxs)(n.p,{children:["We will build a simple app that hosts animating tab selection component in this tutorial. It is inspired by ",(0,a.jsx)(n.a,{href:"https://dribbble.com/shots/14723171-Animated-Tabbar",children:"Cuberto's Animated Tabbar"})," and this ",(0,a.jsx)(n.a,{href:"https://codepen.io/aaroniker/pen/rNMmZvq?editors=0110",children:"JS impl"}),". This app will be multi-platform, which means it will run in the browser and as a desktop application."]}),"\n",(0,a.jsxs)(n.p,{children:["The main focus will be utilizing ",(0,a.jsx)(n.a,{href:"https://nacular.github.io/doodle/docs/animations",children:"Doodle's powerful animation APIs"})," to create smooth transitions with precise timings."]}),"\n",(0,a.jsx)(r.I,{function:"tabStrip",height:"300"}),"\n",(0,a.jsx)(n.admonition,{type:"tip",children:(0,a.jsxs)(n.p,{children:["You can also see the full-screen app here: ",(0,a.jsx)(n.a,{href:"https://nacular.github.io/doodle-tutorials/tabstrip",children:"JavaScript"}),", ",(0,a.jsx)(n.a,{href:"https://nacular.github.io/doodle-tutorials/tabstrip_wasm",children:"WebAssembly"}),"."]})}),"\n",(0,a.jsx)(n.h2,{id:"project-setup",children:"Project Setup"}),"\n",(0,a.jsx)(n.p,{children:"The app will use a Kotlin Multiplatform setup, which means we can run it on a range of targets supported by Doodle. The directory structure follows a fairly common layout, with common classes and resources in one source set and platform-specific items in their own."}),"\n","\n",(0,a.jsx)(n.admonition,{title:"Directory Layout",type:"info",children:(0,a.jsxs)(l.k,{items:u,defaultExpandedItems:["src","src/commonMain"],defaultSelectedItem:"src",children:[(0,a.jsxs)("div",{value:"src",children:[(0,a.jsxs)(n.p,{children:["All source code and resources are located under the ",(0,a.jsx)(n.code,{children:"src"})," directory."]}),(0,a.jsxs)(n.p,{children:["The application logic itself is located in the common source set (",(0,a.jsx)(n.code,{children:"src/commonMain"}),"), which means it is entirely reused for each platform. In fact, the same app is used unchanged (just targeting JS) within this documentation."]})]}),(0,a.jsx)("div",{value:"src/commonMain",children:(0,a.jsxs)(n.p,{children:["Source code and resources for that are usable for platforms are stored in ",(0,a.jsx)(n.code,{children:"commonMain"}),". This app is designed to work on all platforms, so our app code and all logic is found under this directory."]})}),(0,a.jsxs)("div",{value:"src/commonMain/kotlin",children:[(0,a.jsxs)(n.p,{children:["The ",(0,a.jsx)(n.code,{children:"kotlin"})," directory is where all code for a platform resides. In this case, we have all the classes for our app: ",(0,a.jsx)(n.code,{children:"TabStripApp"})," and ",(0,a.jsx)(n.code,{children:"TabStrip"}),"."]}),(0,a.jsx)(n.p,{children:"All of these classes are platform agnostic and used by all targets. This makes our app work on any target Doodle supports."})]}),(0,a.jsx)("div",{value:"src/jsMain",children:(0,a.jsxs)(n.p,{children:["Source code and resources that are needed for Web (JS) target are stored in ",(0,a.jsx)(n.code,{children:"jsMain"}),". Our app is platform agnostic except for the launch portion, which is located in the source below this directory."]})}),(0,a.jsx)("div",{value:"src/jsMain/kotlin",children:(0,a.jsxs)(n.p,{children:["The Web launch portion of our app is located here in the program's ",(0,a.jsx)(n.code,{children:"main"})," function."]})}),(0,a.jsx)("div",{value:"src/jsMain/resources",children:(0,a.jsxs)(n.p,{children:["Holds the ",(0,a.jsx)(n.code,{children:"index.html"})," file that loads the generated JS file produced for the Web (JS) target."]})}),(0,a.jsx)("div",{value:"src/jvmMain",children:(0,a.jsxs)(n.p,{children:["Source code and resources that are needed for Desktop (JVM) target are stored in ",(0,a.jsx)(n.code,{children:"jvmMain"}),"."]})}),(0,a.jsx)("div",{value:"src/jvmMain/kotlin",children:(0,a.jsxs)(n.p,{children:["The Desktop launch portion of our app is located here in the program's ",(0,a.jsx)(n.code,{children:"main"})," function."]})}),(0,a.jsx)("div",{value:"src/wasmJsMain",children:(0,a.jsxs)(n.p,{children:["Source code and resources that are needed for Web (WASM) target are stored in ",(0,a.jsx)(n.code,{children:"wasmJsMain"}),". Our app is platform agnostic except for the launch portion, which is located in the source below this directory."]})}),(0,a.jsx)("div",{value:"src/wasmJsMain/kotlin",children:(0,a.jsxs)(n.p,{children:["The Web launch portion of our app is located here in the program's ",(0,a.jsx)(n.code,{children:"main"})," function."]})}),(0,a.jsx)("div",{value:"src/wasmJsMain/resources",children:(0,a.jsxs)(n.p,{children:["Holds the ",(0,a.jsx)(n.code,{children:"index.html"})," file that loads the generated JS file produced for the Web (WASM) target."]})}),(0,a.jsx)("div",{value:"build.gradle.kts",children:(0,a.jsxs)(n.p,{children:["The ",(0,a.jsx)(n.code,{children:"build.gradle.kts"})," file defines how the app is configured and all its dependencies. The TabStrip app uses a multi-platform configuration so it can run on all Doodle supported targets."]})})]})}),"\n",(0,a.jsxs)(n.p,{children:["Doodle apps are built using gradle like other Kotlin apps. The build is controlled by the ",(0,a.jsx)(n.code,{children:"build.gradle.kts"})," script in the root of the ",(0,a.jsx)(n.code,{children:"TabStrip"})," directory."]}),"\n",(0,a.jsx)(n.p,{children:(0,a.jsx)(n.strong,{children:"build.gradle.kts"})}),"\n",(0,a.jsx)(s.v,{children:c}),"\n",(0,a.jsx)(n.admonition,{type:"info",children:(0,a.jsxs)(n.p,{children:["The gradle build uses ",(0,a.jsx)(n.a,{href:"https://docs.gradle.org/current/userguide/version_catalogs.html",children:"gradle version catalogs"}),"; see ",(0,a.jsx)(n.a,{href:"https://github.com/nacular/doodle-tutorials/blob/master/gradle/libs.versions.toml",children:"libs.versions.toml"})," file for library info."]})}),"\n",(0,a.jsx)(n.h2,{id:"the-application",children:"The Application"}),"\n",(0,a.jsxs)(n.p,{children:["All Doodle apps must implement the ",(0,a.jsx)(n.a,{href:"https://nacular.github.io/doodle/docs/applications",children:(0,a.jsx)(n.code,{children:"Application"})})," interface. The framework will then initialize our app via the constructor. Our app will be fairly simple: just create an instance of our calculator and add it to the display."]}),"\n",(0,a.jsxs)(n.p,{children:["Doodle apps can be defined in ",(0,a.jsx)(n.code,{children:"commonMain"}),", since they do not require any platform-specific dependencies (we will do this as well). They can also be ",(0,a.jsx)(n.a,{href:"https://nacular.github.io/doodle/docs/applications",children:"launched"})," in a few different ways on Web and Desktop. Use the ",(0,a.jsx)(n.code,{children:"application"})," function in a platform source-set (i.e. ",(0,a.jsx)(n.code,{children:"jsMain"}),", ",(0,a.jsx)(n.code,{children:"jvmMain"}),", etc.) to launch top-level apps. It takes a list of modules to load and a lambda that builds the app. This lambda is within a Kodein injection context, which means we can inject dependencies into our app via ",(0,a.jsx)(n.code,{children:"instance"}),", ",(0,a.jsx)(n.code,{children:"provider"}),", etc."]}),"\n",(0,a.jsx)(n.p,{children:(0,a.jsx)(n.a,{href:"https://github.com/nacular/doodle-tutorials/blob/master/TabStrip/src/commonMain/kotlin/io/nacular/doodle/examples/TabStripApp.kt#L11",children:(0,a.jsx)(n.strong,{children:"TabStripApp.kt"})})}),"\n",(0,a.jsx)(s.v,{children:d}),"\n",(0,a.jsx)(n.admonition,{type:"tip",children:(0,a.jsxs)(n.p,{children:["Notice that ",(0,a.jsx)(n.code,{children:"shutdown"})," is a no-op, since we don't have any cleanup to do when the app closes."]})}),"\n",(0,a.jsx)(n.h2,{id:"the-tabstrip-view",children:"The TabStrip View"}),"\n",(0,a.jsxs)(n.p,{children:["This tutorial will implement the ",(0,a.jsx)(n.code,{children:"TabStrip"})," as a single ",(0,a.jsx)(n.a,{href:"https://nacular.github.io/doodle/docs/views",children:(0,a.jsx)(n.code,{children:"View"})})," that manages its state directly. This lets us focus on the animation logic."]}),"\n",(0,a.jsx)(n.admonition,{type:"tip",children:(0,a.jsx)(n.p,{children:"A production version of this control would be more flexible if it let you pass in the items in the tab and configure what each does when clicked."})}),"\n",(0,a.jsxs)(n.p,{children:["The ",(0,a.jsx)(n.code,{children:"TabStrip"})," is composed of a rounded rectangle background (with a drop-shadow), a row of items rendered using paths, an indicator that looks like a wave, and a droplet that appears during a new tab selection. All of these elements are rendered directly onto the ",(0,a.jsx)(n.code,{children:"TabStrip"}),"'s canvas, so there are no child ",(0,a.jsx)(n.code,{children:"View"}),"s involved for this approach."]}),"\n",(0,a.jsx)(n.h3,{id:"render-logic",children:"Render Logic"}),"\n",(0,a.jsxs)(n.p,{children:["All parts of the view are rendered in ",(0,a.jsx)(n.code,{children:"TabStrip.render"}),", which is how all Views draw themselves."]}),"\n",(0,a.jsx)(s.v,{children:"\noverride fun render(canvas: Canvas) {\n  val foreGround = (foregroundColor ?: Black).paint\n  val backGround = (backgroundColor ?: White).paint\n\n  // draw shadow\n  canvas.outerShadow(color = Black opacity 0.1f, blurRadius = 20.0) {\n      // draw background rounded rect\n      canvas.rect(bounds.atOrigin, radius = cornerRadius, fill = backGround)\n  }\n\n  // draw items\n  items.forEach { item ->\n      val itemScale = 1 - itemScaleChange * item.moveProgress\n\n      // position and scale the item\n      canvas.transform(Identity.\n          translate(Point(item.x, item.y + itemDipOffset * item.moveProgress)).\n          scale(around = Point(item.width / 2, item.height / 2), itemScale, itemScale)) {\n\n          when (item.selectionProgress) {\n              1f   -> path(item.selected, fill = foreGround) // fully selected\n              else -> {\n                  path(item.deselected, fill = foreGround)\n\n                  if (item.selectionProgress > 0f) {\n                      // overlay transition if partially selected\n                      val dropletCircle = Circle(\n                          center = Point(item.width / 2, item.height - dropLetRadius),\n                          radius = dropLetRadius + (max(item.width, item.height) - dropLetRadius) * item.selectionProgress\n                      )\n\n                      // overlay background fill so it seeps through holes in item\n                      circle(dropletCircle, fill = backGround)\n\n                      // draw selected item clip to droplet\n                      clip(dropletCircle) {\n                          path(item.selected, fill = foreGround)\n                      }\n                  }\n              }\n          }\n      }\n  }\n\n  canvas.translate(indicatorCenter) {\n      // draw indicator\n      path(indicatorPath, fill = foreGround)\n\n      if (dropletYAboveIndicator != 0.0) {\n          // draw droplet so that it's top is at the indicator top when dropletYAboveIndicator == 0\n          circle(Circle(\n              radius = dropLetRadius,\n              center = Point(0, -indicatorHeight + dropLetRadius - dropletYAboveIndicator)\n          ), fill = foreGround)\n      }\n  }\n}\n"}),"\n",(0,a.jsxs)(n.p,{children:["Notice that the icons and indicator are all drawn after the canvas has been transformed. That is because paths are fixed in space (in our case they are all anchored at ",(0,a.jsx)(n.code,{children:"0,0"}),") and moving them around on a Canvas requires a transform."]}),"\n",(0,a.jsxs)(n.p,{children:["You can also see that each icon can be in 1 of 3 states: ",(0,a.jsx)(n.em,{children:"deselected"}),", ",(0,a.jsx)(n.em,{children:"partially selected"}),", ",(0,a.jsx)(n.em,{children:"fully selected"}),". In the first and last case, only the respective path is drawn. But in the transitional state, both paths are drawn, with the selected path being clipped to the droplet circle. We also need to change the background color that leaks through the holes of the path. So a filler circle is drawn before the selected path with the current background fill."]}),"\n",(0,a.jsx)(n.admonition,{type:"info",children:(0,a.jsxs)(n.p,{children:["Many of the parameters used in ",(0,a.jsx)(n.code,{children:"render"})," are ones we will animate (i.e. ",(0,a.jsx)(n.code,{children:"icon.moveProgress"}),", ",(0,a.jsx)(n.code,{children:"icon.selectionProgress"}),", ",(0,a.jsx)(n.code,{children:"indicatorCenter"}),", ",(0,a.jsx)(n.code,{children:"dropletYAboveIndicator"}),", etc.). Therefore, animation can simply trigger renders to ensure the ",(0,a.jsx)(n.code,{children:"TabStrip"})," always reflects the changes on every tick."]})}),"\n",(0,a.jsx)(n.h3,{id:"triggering-the-animation",children:"Triggering The Animation"}),"\n",(0,a.jsxs)(n.p,{children:["This component has a complex set of animations that will trigger in a specific sequence to achieve the final look. We will use an injected ",(0,a.jsx)(n.code,{children:"Animator"})," names ",(0,a.jsx)(n.code,{children:"animate"})," to perform all animations. And we will tie animations to click events that select a new item."]}),"\n",(0,a.jsxs)(n.p,{children:["The first thing we'll do is track the ",(0,a.jsx)(n.code,{children:"selectedItem"})," as an ",(0,a.jsx)(n.code,{children:"observable"})," property so we can trigger animations when it changes:"]}),"\n",(0,a.jsx)(s.v,{children:"private var selectedItem by observable(items.first()) { _,selected -> ... }"}),"\n",(0,a.jsx)(n.p,{children:"This item will be initialized to the first item in our list. That list will simply be hard-coded for this tutorial. It tracks data for each item in a simple data object."}),"\n",(0,a.jsx)(s.v,{children:"\nprivate inner class ItemState(val selected: Path, val deselected: Path, var selectionProgress: Float = 0f) {\n  val x          get() = position.x\n  val y          get() = position.y\n  val width      get() = size.width\n  val height     get() = size.height\n  val centerX    get() = x + width / 2\n  val size             = pathMetrics.size(selected)\n  val atDefaults get() = selectionProgress == 0f && moveProgress == 0f\n\n  lateinit var position     : Point\n           var moveProgress = 0f\n}\n"}),"\n",(0,a.jsxs)(n.p,{children:["The user is able to change ",(0,a.jsx)(n.code,{children:"selectedItem"})," by clicking on it with the Pointer. We track this by listening to click events on the view directly and deciding which item is selected. We also listen for pointer move events to handle the dynamic cursor, which shows a Pointer when hovering over a non-selected item."]}),"\n",(0,a.jsx)(s.v,{children:"\ninit {\n  // ...\n\n  // Listen for item clicks\n  pointerChanged += clicked { event ->\n      getItem(at = event.location)?.let {\n          selectedItem = it\n          cursor       = Default\n      }\n  }\n\n  // Update cursor as pointer moves\n  pointerMotionChanged += moved { event ->\n      cursor = when (getItem(event.location)) {\n          selectedItem, null -> Default\n          else               -> Pointer\n      }\n  }\n}\n"}),"\n",(0,a.jsxs)(n.p,{children:["The result is that clicking on an item other than ",(0,a.jsx)(n.code,{children:"selectedItem"})," will trigger our observable callback, which is where we do our animation handling."]}),"\n",(0,a.jsx)(n.h3,{id:"animation-timeline",children:"Animation Timeline"}),"\n",(0,a.jsx)(n.p,{children:"This is what the full animation timeline looks like. The diagram shows the sequence of events and their timings (to scale)."}),"\n",(0,a.jsx)(n.p,{children:(0,a.jsx)(n.img,{src:i(7795).A+"",width:"1920",height:"978"})}),"\n",(0,a.jsxs)(n.p,{children:["There are a few important things to note about our approach. First is that we are using an animation block and tracking several top-level animations that will run concurrently via ",(0,a.jsx)(n.code,{children:"animations"}),". This allows us to auto-cancel them whenever a new animation starts. We accomplish this by defining ",(0,a.jsx)(n.code,{children:"animations"})," as an ",(0,a.jsx)(n.code,{children:"autoCanceling"})," property"]}),"\n",(0,a.jsx)(s.v,{children:"private var animation: Animation<*>? by autoCanceling()"}),"\n",(0,a.jsx)(n.admonition,{type:"tip",children:(0,a.jsxs)(n.p,{children:["Use the ",(0,a.jsx)(n.code,{children:"autoCanceling"})," delegate to get free animation cleanup whenever a new value is assigned to an old one."]})}),"\n",(0,a.jsxs)(n.p,{children:["Secondly, our approach initiates a lot of follow-on animations at the completion of previous animations (bubbles that come after others). These are triggered using the ",(0,a.jsx)(n.code,{children:"then"})," method on the animation they follow. This allows them to be tracked by our top-level animation block and they are only created when the previous animation completes."]}),"\n",(0,a.jsxs)(n.admonition,{type:"warning",children:[(0,a.jsxs)(n.p,{children:["Doodle's animation block captures any animations created while the block is being executed and groups them all under a single animation returned from the block. In the following example, all the animations will be tied to ",(0,a.jsx)(n.code,{children:"animations"}),", so canceling it cancels everything."]}),(0,a.jsx)(n.p,{children:(0,a.jsx)(n.strong,{children:"All animations rolled into the result"})}),(0,a.jsx)(s.v,{children:"\nval animations = animate {\n  0f to 1f (using tweenFloat(...)) {}\n  a  to b  (using tweenColor(...)) {} then {\n      ...\n  }\n\n  subAnimation = animate {\n      ...\n  }\n}\n"}),(0,a.jsxs)(n.p,{children:["NOTE that you must use ",(0,a.jsx)(n.code,{children:"then"})," if you'd like to create subsequent animations at the completions of others. Doing the following ",(0,a.jsx)(n.strong,{children:"will not"})," track the animation created in the ",(0,a.jsx)(n.code,{children:"completed"})," callback."]}),(0,a.jsx)(n.p,{children:(0,a.jsx)(n.strong,{children:"Some animations not tracked"})}),(0,a.jsx)(s.v,{children:"\nval animations = animate {\n  (0f to 1f using tweenFloat(...).invoke {}).apply {\n      completed += {\n          // this one not, since it is created after the animate block is finished\n          a  to b  using tweenColor(...).invoke {}\n      }\n  }\n}\n"})]}),"\n",(0,a.jsx)(n.h3,{id:"animation-logic",children:"Animation Logic"}),"\n",(0,a.jsx)(n.p,{children:"The following code has all of the logic to cancel anything that is ongoing when it fires and setup the timeline."}),"\n",(0,a.jsx)(s.v,{children:"\nprivate var selectedItem by observable(items.first()) { _,selected ->\n  // hide droplet\n  dropletYAboveIndicator = 0.0\n\n  // Animation blocks roll all top-level animations (those created while in the block) into a common\n  // parent animation. Canceling that animation cancels all the children.\n  animation = animate {\n      // All deselected items move back to normal\n      items.filter { it != selected && !it.atDefaults }.forEach { deselected ->\n          deselected.moveProgress      to 0f using (tweenFloat(linear, itemMoveUpDuration)) { deselected.moveProgress      = it }\n          deselected.selectionProgress to 0f using (tweenFloat(linear, itemFillDuration  )) { deselected.selectionProgress = it }\n      }\n\n      // Indicator moves to selected item\n      indicatorCenter.x to selected.centerX using (tweenDouble(easeInOutCubic, slideDuration)) { indicatorCenter = Point(it, height) } then {\n          // Selected item moves down\n          selected.moveProgress to 1f using (tweenFloat(linear, itemMoveDownDuration)) { selected.moveProgress = it }\n      }\n\n      // Indicator primes as it travels to selected item\n      indicatorHeight to minIndicatorHeight using (tweenDouble(linear, primeDuration)) { indicatorHeight = it } then {\n          // Indicator fires at selected item\n          indicatorHeight to maxIndicatorHeight using (tweenDouble(linear, fireDuration)) { indicatorHeight = it } then {\n              // Indicator height returns to normal\n              indicatorHeight to defaultIndicatorHeight using (tweenDouble(linear, recoilDuration)) { indicatorHeight = it }\n\n              // Droplet moves up to item\n              dropletYAboveIndicator to dropletMaxY using (tweenDouble(linear, dropletTravelDuration)) { dropletYAboveIndicator = it } then {\n                  // Droplet is instantly hidden\n                  dropletYAboveIndicator = 0.0\n\n                  // Selected item moves up\n                  selected.moveProgress to 0f using (tweenFloat(linear, itemMoveUpDuration)) { selected.moveProgress = it }\n\n                  // Selected item animates droplet within it\n                  selected.selectionProgress to 1f using (tweenFloat(linear, itemFillDuration)) { selected.selectionProgress = it }\n              }\n          }\n      }\n  }\n}\n"}),"\n",(0,a.jsx)(n.h3,{id:"rendering-on-animation",children:"Rendering On Animation"}),"\n",(0,a.jsxs)(n.p,{children:["Our animations change many internal variables as they update. These variables all affect how the control is rendered, and therefore need to trigger re-render so their states are constantly in sync with what the control shows at any moment. One option is to have these variables be ",(0,a.jsx)(n.code,{children:"renderProperty"}),"s. This would trigger a render call whenever any of them is changed. This approach is fine for cases where only a small number of properties will change at a time; then the number of render calls remains low. Doodle does queue calls to ",(0,a.jsx)(n.code,{children:"rerender"}),", but its best to avoid making those calls to begin with if possible."]}),"\n",(0,a.jsxs)(n.p,{children:["So we take a different approach. The ",(0,a.jsx)(n.code,{children:"Animator"})," interface lets you listen to changes to active animations. These events are fired in bulk whenever any animations change, complete, or are canceled. This is a great way to take action at low cost. The code for this is as follows:"]}),"\n",(0,a.jsx)(s.v,{children:"\ninit {\n  // ...\n\n  // Rerender on animation updates\n  animate.listeners += object: Listener {\n      override fun changed(animator: Animator, animations: Set<Animation<*>>) {\n          rerenderNow() // only called once per animation tick\n      }\n  }\n\n  // ...\n}\n"}),"\n",(0,a.jsx)(n.p,{children:"With this, our component will render itself and listen to pointer events so it can trigger animations."}),"\n",(0,a.jsx)(n.h2,{id:"potential-improvements",children:"Potential Improvements"}),"\n",(0,a.jsxs)(n.p,{children:["We focused on the animation logic in this tutorial, but there are other things the ",(0,a.jsx)(n.code,{children:"TabStrip"})," needs to manage given how we've chosen to implement it. These include managing how all the items it draws are positioned relative to its own size."]}),"\n",(0,a.jsx)(n.p,{children:"The approach we chose requires the component to recalculate the offsets of all paths it draws whenever its size changes. That's because everything drawn during render is absolutely positioned on the canvas."}),"\n",(0,a.jsxs)(n.p,{children:["A more production-ready approach might have the component only draw the indicator on its canvas, while having the items as child views. Then it could use a ",(0,a.jsx)(n.a,{href:"https://nacular.github.io/doodle/docs/layout/overview",children:(0,a.jsx)(n.code,{children:"Layout"})})," to keep the item positions up to date. Of course, that ",(0,a.jsx)(n.code,{children:"layout"})," would need to participate in the animation and incorporate the animating values into its positioning logic. But this approach would scale better and allow the control to house a more dynamic set of items."]})]})}function f(e={}){const{wrapper:n}={...(0,o.R)(),...e.components};return n?(0,a.jsx)(n,{...e,children:(0,a.jsx)(b,{...e})}):b(e)}},7795:(e,n,i)=>{i.d(n,{A:()=>t});const t=i.p+"assets/images/animation-full-175d8ba743d7cd7e4b33c83d5d9e09c3.png"}}]);