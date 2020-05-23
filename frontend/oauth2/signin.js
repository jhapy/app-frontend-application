import '@polymer/polymer/polymer-legacy.js';
import {PolymerElement} from '@polymer/polymer/polymer-element.js';
import '@vaadin/vaadin-button/vaadin-button.js';
import {html} from '@polymer/polymer/lib/utils/html-tag.js';

if (!PolymerElement) {
  throw new Error(
      `Unexpected Polymer version ${Polymer.version} is used, expected v2.0.0 or later.`);
}

class Signin extends PolymerElement {
  static get template() {
    return html`
   <vaadin-button on-click="_click" id="signin-button"> 
   </vaadin-button> 
`;
  }

  static get is() {
    return 'sign-in';
  }

  static get version() {
    return '1.0.0';
  }

  static get properties() {
    return {
      providerId: {
        type: String,
        reflectToAttribute: true,
        notify: true
      },
      clientId: {
        type: String,
        reflectToAttribute: true,
        notify: true,
      },
      redirectUri: {
        type: String,
        reflectToAttribute: true,
        notify: true,
      },
      authorization: {
        type: String,
        reflectToAttribute: true,
        notify: true,
      },
      scopes: {
        type: String,
        reflectToAttribute: true,
        notify: true,
      },
      responseType: {
        type: String,
        reflectToAttribute: true,
        notify: true,
      }
    };
  }

  _click() {
    let conf = {
      providerID: this.providerId,
      client_id: this.clientId,
      redirect_uri: (this.redirectUri || 'http://localhost:8080')
          + '/frontend/oauth2/popupCallback.html',
      authorization: this.authorization
    };

    if (this.responseType) {
      conf.response_type = this.responseType;
    }
    if (this.scopes) {
      conf.scopes = {request: JSON.parse(this.scopes)};
    }

    let client = new jso.JSO(conf);

    client.callback();
    client.setLoader(jso.Popup);

    let me = this;

    client.getToken().then(
        (token) => {
          let event = new CustomEvent('oauth-success', {detail: {token: token}});
          me.dispatchEvent(event);
        }
    );
  }
}

customElements.define(Signin.is, Signin);

