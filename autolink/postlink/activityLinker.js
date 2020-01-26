// @ts-check
var path = require('./path');
var fs = require("fs");
var { warnn, logn, infon, debugn } = require("./log");

class ActivityLinker {
  constructor() {
    this.activityPath = path.mainActivityJava;
  }

  link() {
    logn("Linking MainActivity...");
    if (this.activityPath) {
      var activityContent = fs.readFileSync(this.activityPath, "utf8");
      activityContent = this._extendNavigationActivity(activityContent);
      fs.writeFileSync(this.activityPath, activityContent);
      infon("MainActivity linked successfully!\n");
    } else {
      warnn("   MainActivity not found!");
    }
  }

  _extendNavigationActivity(activityContent) {
    var activityPath = path.mainActivityJava;
    if (activityPath) {
      if (this._doesActivityExtendReactActivity(activityContent)) {
        debugn("   Extending NavigationActivity")
        return activityContent
          .replace(/extends\s+ReactActivity\s*/, "extends NavigationActivity ")
          .replace("import com.facebook.react.ReactActivity;", "import com.reactnativenavigation.NavigationActivity;")
      } else {
        warnn("   MainActivity already extends NavigationActivity")
      }
    }
    
    return activityContent;
  }

  _doesActivityExtendReactActivity(activityContent) {
    return /public\s+class\s+MainActivity\s+extends\s+ReactActivity\s*/.test(activityContent);
  }
}

module.exports = ActivityLinker;