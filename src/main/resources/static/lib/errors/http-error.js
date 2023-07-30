/**
 * Represents a failed HTTP request.
 */
export class HttpError extends Error {
  /** @type Response */
  #response

  /**
   * @param {Response} response
   */
  constructor(response) {
    super(`http request failed: ${response.status} ${response.statusText}`)
    this.#response = response
  }

  get response() {
    return this.#response
  }
}
