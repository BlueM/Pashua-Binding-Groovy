#!/usr/bin/env groovy

//
// USAGE INFORMATION:
// As you can see this text, you obviously have opened the file in a text editor.
//
// If you would like to *run* this example rather than *read* it, you
// should open Terminal.app, drag this document's icon onto the terminal
// window, bring Terminal.app to the foreground (if necessary) and hit return.
//

// Define what the dialog should be like
// Take a look at Pashua's Readme file for more info on the syntax
def conf = '''
# Set transparency: 0 is transparent, 1 is opaque
*.transparency = 0.95

# Set window title
*.title = Introducing Pashua

# Introductory text
txt.type = text
txt.default = Pashua is an application for generating dialog windows from programming languages which lack support for creating native GUIs on Mac OS X. Any information you enter in this example window will be returned to the calling script when you hit “OK”; if you decide to click “Cancel” or press “Esc” instead, no values will be returned.[return][return]This window demonstrates nine of the GUI widgets that are currently available. You can find a full list of all GUI elements and their corresponding attributes in the documentation that is included with Pashua.
txt.height = 276
txt.width = 310
txt.x = 340
txt.y = 44

# Add a text field
tf.type = textfield
tf.label = Example textfield
tf.default = Textfield content
tf.width = 310

# Add a filesystem browser
ob.type = openbrowser
ob.label = Example filesystem browser (textfield + open panel)
ob.width=310
ob.tooltip = Blabla filesystem browser

# Define radiobuttons
rb.type = radiobutton
rb.label = Example radiobuttons
rb.option = Radiobutton item #1
rb.option = Radiobutton item #2
rb.option = Radiobutton item #3
rb.option = Radiobutton item #4
rb.default = Radiobutton item #2

# Add a popup menu
pop.type = popup
pop.label = Example popup menu
pop.width = 310
pop.option = Popup menu item #1
pop.option = Popup menu item #2
pop.default = Popup menu item #2

# Add a checkbox
chk1.type = checkbox
chk1.label = Pashua offers checkboxes, too
chk1.rely = -18
chk1.default = 1

# Add another one
chk2.type = checkbox
chk2.label = But this one is disabled
chk2.disabled = 1

# Add a cancel button with default label
cb.type=cancelbutton

'''

// Set the images' paths relative to this file's path /
// skip images if they can not be found in this file's path
def mydir = new File(PashuaRunner.class.protectionDomain.codeSource.location.path).absoluteFile.parentFile
def bgimg = "$mydir/.demo.png";
def icon  = "$mydir/.icon.png";

f = new File(icon)
if (f.exists()) {
	// Display Pashua's icon
	conf += """img.type = image
	           img.x = 530
	           img.y = 255
	           img.path = $icon\n""";
}

f = new File(bgimg)
if (f.exists()) {
	// Display background image
	conf += """bg.type = image
	           bg.x = 30
	           bg.y = 2
	           bg.path = $bgimg""";
}

def result = PashuaRunner.run(conf, 'utf8')
print "Pashua returned the following map:\n"
print result
print "\n"


/**
 * Simple class for using Pashua with Groovy
 * @author Carsten Bluem
 */
class PashuaRunner {

	/**
	 * Main method. Takes a configuration string, displays the dialog by invoking
	 * Pashua and returns the result as a Map. (For information on the configuration
	 * string and the supported encodings, see Pashua's documentation.)
	 * @param conf     Configuration string
	 * @param encoding Configuration encoding (optional; default value: "macroman")
	 * @param appdir   Optional path to the directory containing the Pashua
	 *                 application, if it's not in one of the standard directories
	 * @return         Map containing variable:value pairs
	 * @throws         Throws a java.lang.Exception if Pashua.app could not be found
	 */
	static Map run(conf, encoding = "macroman", appdir = "") {

		def path = findPashua(appdir ? appdir : "")

		if (!path) {
			throw new java.lang.Exception('Unable to locate Pashua.app')
		}

		// Write configuration string to temporary config file
		def temppath = "/usr/bin/mktemp /tmp/Pashua_XXXXXXXX".execute().text.trim()
		def tempfile = new File(temppath)
		tempfile.write(conf)
		tempfile.deleteOnExit()

		// Call pashua binary with config file as argument and read result
		def encodingarg = ""
		if (encoding.toLowerCase() =~ /^[a-zA-Z0-9_]+$/) {
			encodingarg = "-e $encoding"
		}
		def result = "$path $encodingarg $temppath".execute().text.trim()

		// Init result map + parse result
		def vars = [:]
		result.splitEachLine("=", {
			if (it[0].trim().length()) {
				vars[it[0].trim()] = it.tail().join("=")
			}
		})
		return vars
	}

	/**
	 * Locates Pashua.app in one of the common places (including /Applications,
	 * ~/Applications and the current directory) or in the custom directory
	 * given as argument.
	 * @param appdir Optional path to the directory containing the Pashua
	 *               application, if it's not in one of the standard directories
	 * @return       Absolute filesystem path to the executable binary
	 *               inside the application bundle. Empty, if not found
	 */
	protected static String findPashua(appdir = "") {
		def bundlepath = "Pashua.app/Contents/MacOS/Pashua"
 		def dir = new File(PashuaRunner.class.protectionDomain.codeSource.location.path).absoluteFile.parentFile
		def path

		// Try find Pashua in one of the common places
		def paths = [
			"$dir/Pashua", // Inside application bundle
			"$dir/$bundlepath",
			"$bundlepath",
			"/Applications/$bundlepath",
			System.getenv('HOME') + "/Applications/$bundlepath"
		]

		if (appdir) {
			if (appdir.endsWith("/")) {
				appdir = appdir.substring(0, appdir.length() - 1)
			}
			paths.add(0, "$appdir/$bundlepath")
		}

		for (p in paths) {
			def f = new File(p)
			if (f.exists()) {
				if (System.getProperty('java.version') >= '1.6.0') {
					// f.canExecute() requires Java6
					if (!f.canExecute()) {
						continue
					}
				}
				path = p
				break
			}

		}
		return path
	}

}
