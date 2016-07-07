package info.staticfree.android.twentyfourhour.overlay;

/*
 * Copyright (C) 2011-2014 Steve Pomeroy <steve@staticfree.info>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * 20130315 - modified to add Civil, Nautical, Astronomical twilight
 * times by Rob Prior <android@b4.ca>
 *
 */

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.util.Log;

import java.util.Calendar;
import java.util.TimeZone;

import info.staticfree.android.twentyfourhour.lib.R;
import uk.me.jstott.coordconv.LatitudeLongitude;
import uk.me.jstott.sun.Sun;
import uk.me.jstott.sun.Time;

public class SunPositionOverlay implements DialOverlay {

	private static final String TAG = SunPositionOverlay.class.getSimpleName();

	private final LocationManager mLm;

	private final RectF inset = new RectF();
	private final LatitudeLongitude ll = new LatitudeLongitude(0, 0);

	private Location mLocation;

	private static Paint OVERLAY_NO_INFO_PAINT = new Paint(Paint.ANTI_ALIAS_FLAG);

	private static Paint OVERLAY_SUN = new Paint(Paint.ANTI_ALIAS_FLAG);
	private static Paint OVERLAY_NIGHT = new Paint(Paint.ANTI_ALIAS_FLAG);
	private static Paint OVERLAY_CIVIL = new Paint(Paint.ANTI_ALIAS_FLAG);
	private static Paint OVERLAY_NAUTICAL = new Paint(Paint.ANTI_ALIAS_FLAG);
	private static Paint OVERLAY_ASTRO = new Paint(Paint.ANTI_ALIAS_FLAG);

	static {
		OVERLAY_SUN.setARGB(127, 255, 201, 14); // Orange for Sun
		OVERLAY_SUN.setStyle(Paint.Style.FILL);

		OVERLAY_NIGHT.setARGB(20, 0, 0, 0); // Sunrise/Sunset
		OVERLAY_NIGHT.setStyle(Paint.Style.FILL);

		OVERLAY_CIVIL.setARGB(20, 0, 0, 0); // Civil Twilight
		OVERLAY_CIVIL.setStyle(Paint.Style.FILL);

		OVERLAY_NAUTICAL.setARGB(20, 0, 0, 0); // Nautical Twilight
		OVERLAY_NAUTICAL.setStyle(Paint.Style.FILL);

		OVERLAY_ASTRO.setARGB(20, 0, 0, 0); // Astronomical Twilight
		OVERLAY_ASTRO.setStyle(Paint.Style.FILL);
	}

    private float mScale = 0.5f;

    public SunPositionOverlay(Context context) {
		mLm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

		OVERLAY_NO_INFO_PAINT.setShader(new BitmapShader(BitmapFactory.decodeResource(
				context.getResources(), R.drawable.no_sunrise_sunset_tile), Shader.TileMode.REPEAT,
				Shader.TileMode.REPEAT));
	}

	private Location getRecentLocation() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
			return mLm.getLastKnownLocation("passive");

		} else {
			Location bestLoc = null;
			long mostRecent = 0;
			for (final String p : mLm.getProviders(false)) {
				final Location l = mLm.getLastKnownLocation(p);
				if (l == null) {
					continue;
				}
				final long fixTime = l.getTime();
				if (bestLoc == null) {
					bestLoc = l;
					mostRecent = fixTime;
				} else {
					if (fixTime > mostRecent) {
						bestLoc = l;
						mostRecent = fixTime;
					}
				}
			}
			return bestLoc;
		}
	}

    public void setScale(float scale){
        mScale = scale;
    }

    public void setShadeAlpha(int alpha){
        OVERLAY_ASTRO.setAlpha(alpha);
        OVERLAY_CIVIL.setAlpha(alpha);
        OVERLAY_NAUTICAL.setAlpha(alpha);
        OVERLAY_NIGHT.setAlpha(alpha);
    }

	public void setLocation(Location location) {
		mLocation = location;
	}

	public void setUsePassiveLocation() {
		mLocation = null;
	}

	private float getHourArcAngle(int h, int m) {
		return (HandsOverlay.getHourHandAngle(h, m) + 270) % 360.0f;
	}

	private void drawPlaceholder(Canvas canvas) {
		canvas.drawArc(inset, 0, 180, true, OVERLAY_NO_INFO_PAINT);
	}

	@Override
	public void onDraw(Canvas canvas, int cX, int cY, int w, int h, Calendar calendar,
			boolean sizeChanged) {
		final Location loc = mLocation != null ? mLocation : getRecentLocation();
        final int insetW = (int) (w / 2.0f * mScale);
        final int insetH = (int) (h / 2.0f * mScale);
        inset.set(cX - insetW, cY - insetH, cX + insetW, cY + insetH);

		if (loc == null) {
			// not much we can do if we don't have a location
			drawPlaceholder(canvas);
			return;
		}
		ll.setLatitude(loc.getLatitude());
		ll.setLongitude(loc.getLongitude());

		final TimeZone tz = calendar.getTimeZone();

		final boolean dst = calendar.get(Calendar.DST_OFFSET) != 0;

		try {
			final Time morningSunrise = Sun.sunriseTime(calendar, ll, tz, dst);
			final Time morningCivil = Sun.morningCivilTwilightTime(calendar, ll, tz, dst);
			final Time morningNautical = Sun.morningNauticalTwilightTime(calendar, ll, tz, dst);
			final Time morningAstro = Sun.morningAstronomicalTwilightTime(calendar, ll, tz, dst);

			final float morningSunAngle = getHourArcAngle(morningSunrise.getHours(),
					morningSunrise.getMinutes());
			final float morningCivAngle = getHourArcAngle(morningCivil.getHours(),
					morningCivil.getMinutes());
			final float morningNauAngle = getHourArcAngle(morningNautical.getHours(),
					morningNautical.getMinutes());
			final float morningAstAngle = getHourArcAngle(morningAstro.getHours(),
					morningAstro.getMinutes());

			final Time eveningSunset = Sun.sunsetTime(calendar, ll, tz, dst);
			final Time eveningCivil = Sun.eveningCivilTwilightTime(calendar, ll, tz, dst);
			final Time eveningNautical = Sun.eveningNauticalTwilightTime(calendar, ll, tz, dst);
			final Time eveningAstro = Sun.eveningAstronomicalTwilightTime(calendar, ll, tz, dst);

			final float eveningSunAngle = getHourArcAngle(eveningSunset.getHours(),
					eveningSunset.getMinutes());
			final float eveningCivAngle = getHourArcAngle(eveningCivil.getHours(),
					eveningCivil.getMinutes());
			final float eveningNauAngle = getHourArcAngle(eveningNautical.getHours(),
					eveningNautical.getMinutes());
			final float eveningAstAngle = getHourArcAngle(eveningAstro.getHours(),
					eveningAstro.getMinutes());

			final float highNoon = (360 + morningSunAngle + ((360 + (eveningSunAngle - morningSunAngle)) % 360) * 0.5f) % 360;

			canvas.drawArc(inset, eveningSunAngle,
					(360 + (morningSunAngle - eveningSunAngle)) % 360, true, OVERLAY_NIGHT);
			canvas.drawArc(inset, eveningCivAngle,
					(360 + (morningCivAngle - eveningCivAngle)) % 360, true, OVERLAY_CIVIL);
			canvas.drawArc(inset, eveningNauAngle,
					(360 + (morningNauAngle - eveningNauAngle)) % 360, true, OVERLAY_NAUTICAL);
			canvas.drawArc(inset, eveningAstAngle,
					(360 + (morningAstAngle - eveningAstAngle)) % 360, true, OVERLAY_ASTRO);

			if (Math.abs(eveningSunAngle - morningSunAngle) > 0) {
				canvas.drawArc(inset, highNoon - 1, 2, true, OVERLAY_SUN);
			}

			// this can happen when lat/lon and the timezone are out of sync, causing impossible
			// sunrise/sunset times to be calculated.
		} catch (final IllegalArgumentException e) {
			Log.e(TAG, "Error computing sunrise / sunset time", e);
			drawPlaceholder(canvas);
		}
	}
}
