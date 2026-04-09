# iRaceResults API Documentation

This document defines all API endpoints available in the iRaceResults Express application.

## Base URL
The application runs on the configured port (see `appconfig.js`).

## Notes
1. All league IDs are case-insensitive and converted to uppercase internally
2. Valid league IDs are defined in `config.leagueIDs`
3. Some routes set cookies (e.g., `results` sets a `leagueid` cookie)
4. Static files are served from `/script`, `/html`, and `/css` directories
5. League-specific data is stored under `/data/{LEAGUEID}/`
6. All requests are logged with timestamp and URL

---

## Authentication Routes (`/auth/*`)
Handled by `authRoutes.js`. No authentication required unless noted.

### GET /auth/login
Serves the login page. Redirects to `/auth/success` if already authenticated.

### GET /auth/google
Initiates Google OAuth2 flow. Requests `profile` and `email` scopes.

### GET /auth/google/callback
Google OAuth2 callback URL. On success redirects to `/auth/success`; on failure redirects to `/auth/login?error=unauthorized`.

### GET /auth/success
Post-login page showing authorized leagues. Redirects to `/auth/login` if not authenticated.

### GET /auth/logout
Logs out the current user and redirects to `/auth/login`.

### GET /auth/status
Returns current authentication status.

**Response:**
```json
{
  "authenticated": true,
  "user": {
    "email": "user@example.com",
    "displayName": "John Smith",
    "authorizedLeagues": ["LEAGUEID1"],
    "isSuperAdmin": false
  }
}
```
Returns `{ "authenticated": false }` if not logged in.

---

## Global Endpoints

### GET /cache
Returns the entire cached league data. No authentication required.

**Response:**
- Content-Type: `application/json`
- Body: Complete cache object containing all league data

---

### GET /leaguelist
Returns a list of all available league IDs.

**Response:**
- Content-Type: `application/json`
- Body: Array of league identifier strings

---

## Static Asset Endpoints

### GET /trackmaps/:filename
Serves track map PNG files from `data/trackmaps/`.

---

### GET /img/:route
Serves global static image files.

**Supported routes:**
- `leftbar.png`
- `middlebar.png`

---

### GET /:leagueid/img/:route
Serves league-specific image files from `data/{LEAGUEID}/img/`.

**Supported routes:**
- `header.png`
- `footer.png`

**Errors:**
- "Sorry, this is an unknown league." if league ID is invalid

---

## Public League Endpoints

### GET /:leagueid
Serves the main tables page (`tables2.html`) for the league. Redirects `/:leagueid` (no trailing slash) to `/:leagueid/` via 301.

**Errors:**
- "Sorry, this is an unknown league." if league ID is invalid

---

### GET /:leagueid/:route

#### Page Routes

| Route | Description |
|-------|-------------|
| `tables` | Redirects to `/:leagueid` |
| `tables2` / `tables2.html` | Serves `tables2.html` |
| `penalties` | Serves `penalties.html` |
| `results` | Serves `results.html` (sets `leagueid` cookie) |
| `licence` | Serves `licence.html` (sets `leagueid` cookie) |

#### Static File Routes

| Route | File Served |
|-------|-------------|
| `favicon.ico` | `/img/favicon.ico` |
| `bkgrnd.jpg` | `/img/bkgrnd.jpg` |
| `leftbar.png` | `/img/leftbar.png` |
| `middlebar.png` | `/img/middlebar.png` |
| `header.png` | `/data/{LEAGUEID}/img/header.png` |
| `footer.png` | `/data/{LEAGUEID}/img/footer.png` |
| `blank.png` | `/data/trackmaps/blank.png` |
| `style.css` | `/css/style.css` |

#### Analysis & AI Page Routes

| Route | Description |
|-------|-------------|
| `analysis` | Serves `analysis.html` (manual analysis upload viewer) |
| `analysis2` | Serves `analysis2.html` (auto-generated lap time bar charts) |
| `AI` / `ai` | Serves `ai.html` (AI race analysis page) |

#### Analysis & AI Data Routes

**analysis2files**
Returns an array of round numbers that have cached lap time data available for `analysis2`.
```json
[1, 2, 3]
```

**aifiles**
Returns an array of round numbers that have cached AI analysis HTML available.
```json
[1, 2, 3]
```

**analysisfiles**
Returns an array of round numbers that have manually uploaded analysis HTML files.
```json
[1, 2, 3]
```

---

### GET /:leagueid/analysis2_file/R:roundNo
Serves the generated HTML fragment for the auto-generated lap time analysis for the given round. Rendered inside `analysis2.html`.

**Errors:**
- 404 / error page if round data is unavailable

---

### GET /:leagueid/analysis_file/:filename
Serves a manually uploaded analysis HTML file from `data/{LEAGUEID}/analysis/{filename}`.

---

### GET /:leagueid/AI_file/R:roundNo
Serves the cached AI-generated race analysis HTML for the given round number. Generates and caches it on first request.

**Errors:**
- 500 with inline error message if generation fails

---

### GET /:leagueid/AI_regen/R:roundNo
Clears the cached AI analysis for the given round and regenerates it, then serves the fresh HTML.

**Errors:**
- 500 with inline error message if generation fails

---

### GET /:leagueid/laptimes
Returns per-driver lap time data from the cached lap files, suitable for rendering a lap time progression chart.

**Query Parameters:**
- `subsession_id` (required) — iRacing subsession ID
- `cust_id` (required) — iRacing customer ID

**Response (200):**
```json
[
  { "lap": 1, "time": 94.321 },
  { "lap": 2, "time": 93.875 }
]
```
- Only valid laps included (formation/out laps and laps with `lap_time <= 0` are filtered out)
- `time` is in seconds, rounded to 3 decimal places
- `lap` is the `lap_number` from the raw iRacing data (1-based)

**Errors:**
- `400` — `subsession_id` or `cust_id` missing
- `404` — league not found, or no cached lap data file for that session/driver

**File Location:** `data/{LEAGUEID}/laptimes/{subsession_id}-{cust_id}.json`

---

#### Data API Routes

**leaguename**
Returns the league's display name.
```json
{ "leagueid": "LEAGUEID", "leaguename": "My League Name" }
```

**colortheme**
Returns the league's configured color theme.
```json
{ "leagueid": "LEAGUEID", "colortheme": "green" }
```
Possible values: `"green"`, `"orange"`, `"blue"`, `"red"`, `"purple"`. Defaults to `"green"`.

**leagueid**
Returns the league name (legacy endpoint).
```json
"My League Name"
```

**displayconfig**
Returns display configuration for the tables UI.

**classtotals**
Returns filtered class championship standings. Async.

**teamstotals**
Returns team championship standings from cache.

**licencepoints**
Returns licence points data from cache.

**fullresults**
Returns complete results data from cache.

**classes**
Returns class configuration array from cache.

**protests**
Returns protests array from cache.

**drivers**
Returns all drivers in the league from cache.

**driverlist**
Alias for `drivers`.

**rounds**
Returns the rounds array.

**penaltiesjson**
Returns the penalties array from cache.

**completedrounds**
Returns completed rounds array.

**lastrecalc**
Returns the most recent recalculation log entry, or `null`.
- Error: 500 if retrieval fails.

**reload**
Reloads cache from saved files for the league.
- Response: `"reloaded Cache for {LEAGUEID}"`

---

### POST /:leagueid/:route

#### POST /:leagueid/map
Returns a track map image for the given round name. Looks up available tracks dynamically from `data/tracks.json`.

**Request Body:**
```json
{ "round_name": "Spa" }
```

**Response:**
- Content-Type: `image/png`
- Returns matching track map file from `data/trackmaps/{round_name}.lowercase().png`
- Returns `blank.png` if `round_name` is `"none"`
- Returns `nomap.png` if track map file not found

---

#### POST /:leagueid/results
Returns filtered results for a specific round and/or driver.

**Request Body:**
```json
{
  "round_no": 3,
  "cust_id": 12345,
  "view": "class"
}
```
- `view`: `"class"` (default) or `"overall"` — controls whether results use class or overall positions

**Response:** Filtered results array (JSON)

---

#### POST /:leagueid/irresults
Returns the raw iRacing session results JSON file.

**Request Body:**
```json
{ "round_no": 3, "session_no": 0 }
```

**Response:** iRacing session results JSON
- Error: `"No such round/session"` if session ID is 0

**File Location:** `/data/{LEAGUEID}/irresults/{SESSION_ID}.json`

---

#### POST /:leagueid/off-track
Receives an off-track event from an external source (e.g. iRacing telemetry client) and appends it to the league's off-track log file for that session.

**Request Body:**
```json
{
  "subsession_id": 84334068,
  "session_type": "RACE",
  "cust_id": 1016431,
  "lap_no": 5,
  "track_pct": 0.312
}
```
All five fields are required.

**Response (200):**
```json
{ "status": "ok", "count": 42 }
```
`count` is the total number of off-track events stored for this session.

**Errors:**
- `400` — any required field missing
- `500` — file system error

**File Location:** `data/{LEAGUEID}/offtracks/{subsession_id}-offtracks.json`

---

#### POST /:leagueid/driverclass
Returns a driver's class history and current class after applying all class changes.

**Request Body:**
```json
{ "cust_id": 12345, "round_no": 5 }
```
- `round_no` is optional; if omitted returns current class after all changes applied

**Success Response (200):**
```json
{
  "cust_id": 12345,
  "display_name": "John Smith",
  "original_class": 2,
  "current_class": 3,
  "class_changes_applied": [
    { "cust_id": 12345, "display_name": "John Smith", "new_class_number": 3, "change_from_round": 4 }
  ]
}
```

**Error Response (404):**
```json
{ "error": "Driver not found", "cust_id": 99999 }
```

---

## Admin Routes (`/admin/*`)
Handled by `admin.js`. All routes require Google OAuth authentication. League-specific routes additionally require authorization for that league. Archived leagues (status 3) are blocked from admin access.

### GET /admin/
Serves the admin home page (`admin_home.html`) — league selector for logged-in user.

### GET /admin/myleagues
Returns the list of leagues the authenticated user is authorized to administer.

**Response:**
```json
[
  { "id": "LEAGUEID", "name": "My League", "status": 1 }
]
```

### GET /admin/:leagueid
Serves the league admin dashboard (`admin.html`). Redirects to trailing-slash URL if needed.

### GET /admin/:leagueid/:route

#### HTML Page Routes (all set `leagueid` cookie)

| Route | Page Served |
|-------|-------------|
| `stewarding` | `stewarding.html` |
| `stewardspen` | `stewardspen.html` |
| `penalties_admin` | `penalties_admin.html` |
| `protests_admin` | `protests_admin.html` |
| `recalc_admin` | `recalc_admin.html` |
| `licencepoints_admin` | `licencepoints_admin.html` |
| `classchanges_admin` | `classchanges_admin.html` |
| `teams_admin` | `teams_admin.html` |
| `incidents_admin` | `incidents_admin.html` |
| `session` | `session.html` |
| `loglist` | `loglist.html` |
| `config` | `config.html` |
| `grid` | `grid_admin.html` |
| `ai_admin` | `ai_admin.html` (AI analysis admin — list rounds, trigger regeneration) |

#### Static File Routes

| Route | File Served |
|-------|-------------|
| `style.css` | `/css/style.css` |
| `header.png` | `/data/{LEAGUEID}/img/header.png` |

#### Data API Routes

| Route | Description | Response |
|-------|-------------|----------|
| `leagueid` | Returns `{ "leagueid": "LEAGUEID" }` | JSON |
| `leaguename` | Returns `{ "leagueid": "...", "leaguename": "..." }` | JSON |
| `protests` | Protests array | JSON |
| `allprotests` | All protests array (alias) | JSON |
| `unresolvedprotests` | Unresolved protests only | JSON |
| `sessions` | Sessions detail | JSON |
| `teamsjson` | Teams array (or `[]` if none) | JSON |
| `validateteams` | Validates teams; auto-corrects driver name mismatches. Returns `{ valid, errors, corrections, teamCount }` | JSON |
| `completedrounds` | Completed rounds | JSON |
| `classes` | Classes array | JSON |
| `driverlist` | Drivers array | JSON |
| `penaltiesjson` | Penalties array | JSON |
| `licencepoints` | Licence points array | JSON |
| `classchangesjson` | Class changes array | JSON |
| `configjson` | Full league config object | JSON |
| `recalculationlog` | In-memory recalculation log | JSON |
| `discordtablesupdated` | Sends Discord webhook message that tables have been updated | `{ "confirmation": "ok" }` |
| `drivers` | Driver HTML (legacy editor format) | HTML |
| `checkofftracks?subsession_id=N` | Checks if an off-track file exists for the given session | `{ "hasFileData": true, "count": 42 }` or `{ "hasFileData": false, "count": 0 }` |

### POST /admin/:leagueid/:route

#### Driver Management

**scoredevents** — Returns scored events for a round.
```json
// Request
{ "round_no": 3 }
```

**driver** — Looks up a driver by `cust_id`. Fetches from iRacing API if not in local database.
```json
// Request
{ "cust_id": 12345 }
```

**adddriver** — Adds a new driver to the league.
```json
// Request
{ "cust_id": 12345, "display_name": "John Smith", "custom_display_name": "", "classnumber": 1 }
// Response
{ "confirmation": "modified driver record saved successfuly" }
```

**moddriver** — Updates an existing driver record.
```json
// Request: driver object with cust_id
// Response: { "confirmation": "modified driver record saved successfuly" }
```

**deletedriver** — Deletes a driver by `cust_id`.
```json
// Request
{ "cust_id": 12345 }
// Response
{ "confirmation": "driver deleted successfully" }
```

#### Session Management

**updatesession** — Updates session ID for a round/session.
```json
// Request
{ "session_ref": 1, ... }
// Response
{ "confirmation": "modified session record saved successfully" }
```

#### Penalty Management

**penalty** — Submits a new penalty.
```json
// Request: { "penalty": "<JSON string>" }
// Response: { "confirmation": "Penalty submitted Successfully" }
```

**stewardspenalty** — Submits a stewards' decision penalty.
```json
// Request: { "penalty": "<JSON string>" }
// Response: { "confirmation": "ok" }
```

**incidentpenalties** — Bulk-creates time penalties from incident data (CSV import workflow).
```json
// Request
{
  "round_no": 3,
  "session_no": 0,
  "score_event": "Feature Race",
  "penalties": [
    { "cust_id": 12345, "time_added": 5, "incidents": 5 }
  ]
}
// Response
{ "confirmation": "ok", "count": 1 }
```
Each penalty is saved with `stewards_decision: "Automatic incident penalty"`.

**updatepenalty** — Updates an existing penalty. Requires `penalty_id`.
```json
// Response: { "confirmation": "ok" }
```

**deletepenalty** — Deletes a penalty by `penalty_id`.
```json
// Request: { "penalty_id": "<id>" }
// Response: { "confirmation": "ok" }
```

**penaltiesjson** — Returns penalties array (POST alias of GET route).

#### Config Management

**updateconfig** — Saves updated league config.
```json
// Request: config object
// Response: { "confirmation": "Config updated successfully" }
```

#### Protest Management

**resolveprotest** — Marks a protest as resolved.
```json
// Request: { "protest_id": "<id>" }
// Response: { "confirmation": "ok" }
```

**unresolveprotest** — Reverts a protest to unresolved.
```json
// Request: { "protest_id": "<id>" }
// Response: { "confirmation": "ok" }
```

#### Discord

**discordmessage** — Sends a custom message to the league's Discord webhook.
```json
// Request: { "message": "Hello Discord" }
// Response: { "confirmation": "ok" }
```

#### Class Changes

**addclasschange** — Adds a driver class change.
```json
// Request: { "cust_id": 12345, "new_class_number": 2, "change_from_round": 5 }
// Response: { "confirmation": "Class change added successfully" }
```

**updateclasschange** — Updates a class change by index.
```json
// Request: { "index": 0, "cust_id": 12345, "new_class_number": 2, "change_from_round": 5 }
// Response: { "confirmation": "Class change updated successfully" }
```

**deleteclasschange** — Deletes a class change by index.
```json
// Request: { "index": 0 }
// Response: { "confirmation": "Class change deleted successfully" }
```

#### Team Management

**addteam** — Creates a new team.
```json
// Request: { "team_name": "Team A", "drivers": [{ "cust_id": 12345, "display_name": "..." }] }
// Response: { "confirmation": "Team added successfully" }
```

**updateteam** — Updates a team by index.
```json
// Request: { "index": 0, "team_name": "Team A", "drivers": [...] }
// Response: { "confirmation": "Team updated successfully" }
```

**deleteteam** — Deletes a team by index.
```json
// Request: { "index": 0 }
// Response: { "confirmation": "Team deleted successfully" }
```

#### Off-Track Data

**fetchofftracks** — Fetches off-track counts for a session, either from the cached file or live from the iRacing API.
```json
// Request
{ "subsession_id": 84334068, "source": "file" }
```
- `source`: `"file"` forces file data; `"iracing"` forces API fetch; omit to auto-detect (uses file if present)

**Response:**
```json
{
  "source": "file",
  "results": [
    { "cust_id": 1016431, "display_name": "John Smith", "off_track_count": 3 }
  ]
}
```
- `source` in response is `"file"` or `"iracing"` indicating which was used
- Driver names resolved from league driver cache when using file data

**Errors:**
- `400` — `subsession_id` missing
- `500` — file/API error

---

#### Grid & Recalculation

**griddata** — Returns championship-order grid data for all classes, based on reverse standings from the most recent Feature race.
```json
// Response: Array of { gridPosition, classname, classnumber, classPosition, driverName, championshipPosition, championshipPoints, previousRaceParticipation }
```

**recalculate** — Triggers a full championship recalculation. Blocked if league status is Completed (2).
```json
// Request: { "reason_for_recalculation": "Penalty applied", "post_to_discord": "TRUE" }
// Response: { "confirmation": "recalculated LEAGUEID", "formattedDate": "...", "userDisplayName": "...", "userEmail": "...", "reason": "..." }
```

---

## Superadmin Routes (`/superadmin/*`)
Handled by `superadmin.js`. All routes require superadmin privileges.

### GET /superadmin/
Serves the superadmin page (`superadmin.html`).

### GET /superadmin/leagues
Returns all leagues with their name and status.
```json
[
  { "leagueID": "LEAGUEID", "leagueName": "My League", "leagueStatus": 1 }
]
```

### POST /superadmin/createleague
Creates a new league by copying the TEMPLATE directory.
```json
// Request: { "leagueID": "NEWLEAGUE", "leagueName": "New League Name" }
// Response: { "confirmation": "League created successfully" }
```

### POST /superadmin/deleteleague
Removes a league from `config.json` and cache. Data directory is preserved.
```json
// Request: { "leagueID": "LEAGUEID" }
// Response: { "confirmation": "League removed from config (data directory preserved)" }
```

### POST /superadmin/updateleaguestatus
Updates a league's status value.
```json
// Request: { "leagueID": "LEAGUEID", "leagueStatus": 2 }
// Response: { "confirmation": "League status updated successfully" }
```
Status values: `0` = Hidden, `1` = Active, `2` = Completed, `3` = Archived.

### GET /superadmin/points/:leagueID
Returns `points.json` for the specified league.

### POST /superadmin/points/:leagueID
Saves `points.json` for the specified league and updates cache.
```json
// Response: { "confirmation": "Points saved successfully" }
```

### GET /superadmin/scoring/:leagueID
Returns `scoring.json` for the specified league.

### POST /superadmin/scoring/:leagueID
Saves `scoring.json` for the specified league and updates cache.
```json
// Response: { "confirmation": "Scoring saved successfully" }
```

### GET /superadmin/rounds/:leagueID
Returns `rounds.json` for the specified league.

### POST /superadmin/rounds/:leagueID
Saves `rounds.json` for the specified league and updates cache.
```json
// Response: { "confirmation": "Rounds saved successfully" }
```

### GET /superadmin/classes/:leagueID
Returns `classes.json` for the specified league.

### POST /superadmin/classes/:leagueID
Saves `classes.json` for the specified league and updates cache.
```json
// Response: { "confirmation": "Classes saved successfully" }
```

### GET /superadmin/adminusers/:leagueID
Returns `adminusers.json` for the specified league, or `[]` if file doesn't exist.
```json
[
  { "email": "user@gmail.com", "name": "John Smith" }
]
```

### POST /superadmin/adminusers/:leagueID
Saves `adminusers.json` for the specified league and updates cache.
```json
// Response: { "confirmation": "Admin users saved successfully" }
```

### GET /superadmin/tracks
Returns global `data/tracks.json`.

### POST /superadmin/tracks
Saves global `data/tracks.json`.
```json
// Response: { "confirmation": "Tracks saved successfully" }
```

### POST /superadmin/uploadtrackmap
Uploads a track map PNG image. Image must be base64-encoded.
```json
// Request: { "shortName": "Spa", "imageData": "data:image/png;base64,..." }
// Response: { "confirmation": "Track map uploaded successfully" }
```
Saves to `data/trackmaps/{shortName.toLowerCase()}.png`.

### GET /superadmin/validate-trackmaps
Validates track map coverage for active leagues (status 1). Checks rounds from the last 7 days and upcoming rounds only.

**Response:**
```json
{
  "missingFromTracksList": [
    { "trackName": "Fuji", "usedByLeagues": ["LEAGUEID1"] }
  ],
  "missingTrackMapFiles": [
    { "fullName": "Fuji International Speedway", "shortName": "Fuji" }
  ]
}
```

---

## Protest Routes (`/:leagueid/protest/*`)
Handled by `protest.js`. See `protest.js` for full endpoint details.

## Registration Routes (`/:leagueid/register/*`)
Handled by `register.js`. See `register.js` for full endpoint details.

---

## Error Responses

| Condition | Response |
|-----------|----------|
| Invalid league ID | `"Sorry, this is an unknown league."` |
| Unknown GET route | `"UNKNOWN ROUTE : The leagueid you specified is {LEAGUEID} and the route requested is :{ROUTE}"` |
| Unknown POST route | `"UNKNOWN POST ROUTE : The leagueid you specified is {LEAGUEID} and the route requested is :{ROUTE}"` |
| Unknown URL (catch-all) | `"Sorry, this is an unknown URL."` |
| Archived league admin access | HTTP 403 `"This league has been archived and is no longer accessible through the admin interface."` |
| Recalculate on completed league | HTTP 403 `{ "error": "Cannot recalculate a completed league..." }` |

---

## Known Issues
- Protest numbering sequence incorrect
- If more than one round is open for protest, events for only the first round are shown
- Dividing lines are missing in scores table
