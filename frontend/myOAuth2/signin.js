import '@polymer/polymer/polymer-legacy.js';
import {PolymerElement} from '@polymer/polymer/polymer-element.js';
import '@vaadin/vaadin-button/vaadin-button.js';
import {html} from '@polymer/polymer/lib/utils/html-tag.js';

if (!PolymerElement) {
  throw new Error(
      `Unexpected Polymer version ${Polymer.version} is used, expected v2.0.0 or later.`);
}

class Signin2 extends PolymerElement {
  static get template() {
    return html`
   <vaadin-button on-click="_click" id="signin-button"> 
   </vaadin-button> 
`;
  }

  static get is() {
    return 'sign-in-2';
  }

  static get version() {
    return '1.0.0';
  }

  static get properties() {
    return {
      authUrl: {
        type: String,
        reflectToAttribute: true,
        notify: true
      },
      redirectUri: {
        type: String,
        reflectToAttribute: true,
        notify: true,
      }
    };
  }

  _click() {

    let me = this;

    window.popupCompleted = function () {
      var url_string = newwindow.location.href;
      var url = new URL(url_string);
      var accessToken = url.searchParams.get("access_token");
      let event = new CustomEvent('oauth-success', {detail: {token: accessToken}});
      me.dispatchEvent(event);
    }

    var newwindow = window.open(this.authUrl, 'jso-popup-auth', 'height=600,width=800')
    if (newwindow === null) {
      throw new Error("Error loading popup window")
    }
    if (window.focus) {
      newwindow.focus()
    }

    /*
    let conf = {
        client_id: 0,
        authorization: this.authUrl,
        redirect_uri: (this.redirectUri || 'http://localhost:8080') + '/frontend/oauth2/popupCallback.html',
    };

    let client = new jso.JSO(conf);

    client.callback();
    client.setLoader(jso.Popup);

    let me = this;

    client.getToken().then(
        (token) => {
            client.dump();
            let event = new CustomEvent('oauth-success', {detail:{token:token}});
            me.dispatchEvent(event);
        }
    );
     */
  }
}

customElements.define(Signin2.is, Signin2);

